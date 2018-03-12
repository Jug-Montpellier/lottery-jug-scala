package lottery

import akka.actor.{Actor, ActorLogging}

/**
 * Created by chelebithil on 14/11/2016.
 */
class LotteryApp extends Actor with ActorLogging {
  override def receive = {

    case e =>
      log.warning(s"WTF $e")
  }
}
