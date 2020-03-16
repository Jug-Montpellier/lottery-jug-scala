package mthlotto

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import mthlotto.server.LottoServer
import org.slf4j.LoggerFactory

object Main {
  def apply(): Behavior[LottoServer.ServerStarted] =
    Behaviors.setup[LottoServer.ServerStarted](context => new Main(context))

}

class Main(context: ActorContext[LottoServer.ServerStarted])
    extends AbstractBehavior[LottoServer.ServerStarted](context) {

  context.log.info("Start")

  val son = context.spawn(LottoServer(), "server")

  sys.addShutdownHook({
    context.log.info("Graceful stop")
    son ! LottoServer.Stop
    context.system.terminate()

  })

  son ! LottoServer.StartServer(context.self)

  override def onMessage(msg: LottoServer.ServerStarted): Behavior[LottoServer.ServerStarted] = msg match {
    case LottoServer.ServerStarted(port) =>
      context.log.info(s"Server started listening on $port")
      Behaviors.same
  }

}

object LottoMain extends App {

  val logger = LoggerFactory.getLogger(getClass())

  ActorSystem[LottoServer.ServerStarted](Main(), "MTHLottoSystem")
}
