package mthlotto.server

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext.Implicits.global

object LottoServer {

  def apply(): Behavior[ServerMessage] =
    Behaviors.setup(context => new LottoServer(context))

  sealed trait ServerMessage

  case class StartServer(replyTo: ActorRef[ServerStarted]) extends ServerMessage
  case class ServerStarted(port: Int)

}

class LottoServer(context: ActorContext[LottoServer.ServerMessage])
    extends AbstractBehavior[LottoServer.ServerMessage](context)
    with ResourceHelper {

  import akka.actor.typed.scaladsl.adapter._
  import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

  implicit val system = context.system.toClassic
  implicit val materializer = ActorMaterializer()

  val route = cors() {
    path("") {
      get {
        streamFromClasspath("/public/index.html")
      }
    } ~
    path("ping") {
      complete("Pong")
    } ~
    path("diagram" / Remaining) { fileName =>
      get {
        streamFromClasspath(s"/diagram/$fileName")
      }
    }
  }

  import LottoServer._

  override def onMessage(
      msg: LottoServer.ServerMessage
  ): Behavior[ServerMessage] = msg match {
    case StartServer(replyTo) =>
      Http()
        .bindAndHandle(route, "0.0.0.0", 8888)
        .foreach { httpBinding =>
          sys.addShutdownHook({
//            log.("Graceful stop")
            httpBinding.unbind().foreach { _ =>
              system.terminate()
            }
          })
        }
      replyTo ! ServerStarted(8888)
      this
  }

  override def onSignal: PartialFunction[Signal, Behavior[ServerMessage]] = {
    case PostStop =>
      context.log.info("Stopped")
      this
  }
}
