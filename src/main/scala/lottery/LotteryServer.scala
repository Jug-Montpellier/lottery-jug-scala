package lottery

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.StandardRoute
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters

import scala.language.implicitConversions
import de.heikoseeberger.akkahttpcirce.CirceSupport._
import io.circe.generic.auto._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import StatusCodes._
import Directives._
import akka.http.javadsl.model.headers.{AccessControlAllowMethods, AccessControlAllowOrigin}
import akka.http.scaladsl.model.headers.HttpOriginRange

import scala.io.StdIn

object WebServer extends App {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher


  val extenstionExtractor = "(?!\\.)[a-z]+$$".r


  implicit def htmlUTF8(content: String): StandardRoute = complete(HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, content)))

  /**
    * Complete the request by streaming a classpath content.
    *
    * @param path
    * @return
    */
  def streamFromClasspath(path: String): StandardRoute = {
    val entity = for {
      ext <- extenstionExtractor.findFirstIn(path)
      contentType <- ext match {
        case "html" => Some(MediaTypes.`text/html`.withCharset(HttpCharsets.`UTF-8`))
        case "png" => Some(MediaTypes.`image/png`.toContentType)
        case _ => None
      }
      inputStream <- Option(getClass.getResourceAsStream(path))
    } yield HttpEntity(contentType = contentType, StreamConverters.fromInputStream(() => inputStream))

    entity.map(complete(_))
      .getOrElse(
        complete(NotFound, "Nope"))
  }


  val lottery = system.actorOf(Props[Lottery], "lottery")

  val route =
    path("") {
      get {
        streamFromClasspath("/public/index.html")
      }
    } ~
      path("winners") {
        get {
          parameters('nb.as[Int]) {
            (n: Int) =>
              respondWithHeaders(AccessControlAllowOrigin.create(HttpOriginRange.*), AccessControlAllowMethods.create(HttpMethods.GET, HttpMethods.OPTIONS)) {

                completeWith(implicitly[ToResponseMarshaller[List[Attendeed]]]) {
                  cb =>
                    lottery ! LotteryProtocol.WinnerRequest(None, n, Some(cb))
                }
              }
          }
        }
      } ~ path("diagram" / Remaining) {
      fileName =>
        get {
          streamFromClasspath(s"/diagram/$fileName")
        }
    }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

  println(s"Server online at http://localhost:8080/\nPress Ctrl-C or send SIGTERM to stop...")

  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

  sys.addShutdownHook({
    println("Graceful stop")
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  })

}
