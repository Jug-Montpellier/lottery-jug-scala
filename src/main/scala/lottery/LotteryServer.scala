package lottery

import akka.actor.{ ActorSystem, Props }
import akka.stream.ActorMaterializer

import scala.io.StdIn
import scala.language.implicitConversions

object WebServer extends App {

  implicit val system       = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val lottery =
    system.actorOf(Props(new Lottery(Props[LotteryHttpServer])), "lottery")

  //StdIn.readLine() // let it run until user presses return

  //lottery ! LotteryProtocol.Stop
}
