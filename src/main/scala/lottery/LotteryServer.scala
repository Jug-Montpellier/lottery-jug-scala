package lottery

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.stream.{ActorMaterializer, SourceShape}
import akka.stream.scaladsl.{FileIO, Source, StreamConverters}
import com.sun.imageio.spi.OutputStreamImageOutputStreamSpi

import scala.io.StdIn
import scala.language.implicitConversions
import de.heikoseeberger.akkahttpcirce.CirceSupport._
import io.circe.generic.auto._

object WebServer extends App {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  implicit def htmlUTF8(content: String): StandardRoute = complete(HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, content)))

  val lottery = system.actorOf(Props[Lottery], "lottery")


  val route =
    path("") {
      get {
        complete(HttpEntity(contentType = MediaTypes.`text/html`.withCharset(HttpCharsets.`UTF-8`), StreamConverters.fromInputStream(() => getClass.getResourceAsStream(s"/public/index.html"))))
      }
    } ~
      path("winners") {
        get {
          parameters('n.as[Int]) {
            (n: Int) =>
              completeWith(implicitly[ToResponseMarshaller[List[Attendeed]]]) {
                cb =>
                  lottery ! LotteryProtocol.WinnerRequest("28525090313", n, cb)
              }
          }
        }
      } ~ path("diagram" / Remaining) {
      fileName =>
        get {
          complete(HttpEntity(contentType = MediaTypes.`image/png`, StreamConverters.fromInputStream(() => getClass.getResourceAsStream(s"/diagram/$fileName"))))
        }
    }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

  println(s"Server online at http://localhost:8080/\nPress Ctrl-C or send SIGTERM to stop...")

  sys.addShutdownHook({
    println("Graceful stop")
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  })

}
