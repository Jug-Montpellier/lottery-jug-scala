package mthlotto

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Main {
  def apply(): Behavior[LottoServer.ServerStarted] =
    Behaviors.setup[LottoServer.ServerStarted](context => new Main(context))

}

class Main(context: ActorContext[LottoServer.ServerStarted])
    extends AbstractBehavior[LottoServer.ServerStarted](context) {

  context.log.info("Start")

  val son = context.spawn(LottoServer(), "server")

  son ! LottoServer.StartServer(context.self)

  override def onMessage(msg: LottoServer.ServerStarted): Behavior[LottoServer.ServerStarted] = msg match {
    case LottoServer.ServerStarted(port) =>
      context.log.info(s"Server started listening on $port")
      Behaviors.unhandled
  }

}

object LottoMain extends App {
  ActorSystem[LottoServer.ServerStarted](Main(), "MTHLottoSystem")
}
