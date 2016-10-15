package lottery

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.concurrent.duration.DurationInt
import scala.util.Random.shuffle

import scala.concurrent.ExecutionContext.Implicits.global

object LotteryProtocol {

  case class WinnerRequest(eventId: String, number: Int, cb: List[Attendeed] => Unit)
  case class AttendeesRequest(sender: ActorRef, request: WinnerRequest)
  case class AttenteesResponse(request: WinnerRequest, attendees: List[Attendeed])
  case class ClearCache(eventId: String)

}

class Lottery extends Actor with ActorLogging {

  import  LotteryConf.cacheTTL
  import LotteryProtocol._

  var cache = Map[String, List[Attendeed]]()

  def take(n: Int, attentees: List[Attendeed]) = shuffle(attentees).take(n)

  override def receive: Receive = {
    case winnerrequest @ WinnerRequest(eventId, n, cb) =>
      cache.get(eventId)
        .map{
          attendees =>
            cb(take(n, attendees))}
        .orElse {
          log.info("Asking event brite...")
          context.actorOf(Props(new LotteryRequester(AttendeesRequest(self, winnerrequest))), "requestor")
          None
        }

    case AttenteesResponse(WinnerRequest(eventId, number, cb), attendees) =>
      cache +=  eventId -> attendees
      cb(take(number, attendees))
      //Schedule a cache eviction.
      context.system.scheduler.scheduleOnce(cacheTTL seconds, self, ClearCache(eventId))

    case ClearCache(eventId) =>
      cache -= eventId
      log.info(s"EventId: $eventId cleared from cache")

    case e =>
      log.warning(s"WTF: $e")
  }
}
