package lottery

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.stream.ActorMaterializer

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

  val route =
    path("") {
      get {
        completeWith(implicitly[ToResponseMarshaller[List[Attendeed]]]) {
          cb =>
            system.actorOf(Props(new EventBriteHttpClient(cb))) ! "28525090313"
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
