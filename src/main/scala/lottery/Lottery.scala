package lottery

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}

import scala.concurrent.duration.DurationInt
import scala.util.Random.shuffle
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.pipe


object LotteryProtocol {

  case class WinnerRequest(eventId: Option[String], number: Int, cb: Option[List[Attendeed] => Unit])

  case class AttendeesRequest(sender: ActorRef, request: WinnerRequest)

  case class AttenteesResponse(request: WinnerRequest, attendees: List[Attendeed])

  case class RefreshCache(winnerRequest: WinnerRequest)

  case object RefreshCurrentEventId

}

class Lottery extends Actor with ActorLogging {

  import LotteryConf._
  import LotteryProtocol._

  private val eventsURI = s"https://www.eventbriteapi.com/v3/events/search/?organizer.id=$organizerId&token=$token"

  private var currentEventId: Option[String] = None

  private var cache = Map[String, List[Attendeed]]()

  override def preStart(): Unit = {
    super.preStart()
    self ! RefreshCurrentEventId
  }

  def take(n: Int, attentees: List[Attendeed]) = shuffle(attentees).take(n)

  override def receive: Receive = {
    case EventPage(pagination, events) =>
      if (events.size == 0) {
        currentEventId = None
        log.warning("No opened event!")
      }
      else {
        currentEventId = Some(events(0).id)
        if (events.size > 1)
          log.warning(s"More than one event is open !\n choosing ${events(0)}")
        else
          log.info(s"Current event ${events(0)}")
        self ! RefreshCache(WinnerRequest(currentEventId, 0, None ))
      }


    case winnerrequest@WinnerRequest(eventId, n, cb) => eventId.orElse(currentEventId).map {
      eventId =>
        cache.get(eventId)
          .map {
            attendees =>
              cb.map(_(take(n, attendees)))
          }
          .orElse {
            log.info("Asking event brite...")
            context.actorOf(Props(new LotteryRequester(AttendeesRequest(self, winnerrequest.copy(eventId=Some(eventId))))), "requestor")
            None
          }
    }.orElse {
      log.warning("No opened event!")
      cb.map(_(Nil))
      None
    }

    case AttenteesResponse(WinnerRequest(eventId, number, cb), attendees) => eventId.map {
      eventId =>
        cache += eventId -> attendees
        cb.map(_(take(number, attendees)))
        //Schedule a cache eviction.
        context.system.scheduler.scheduleOnce(cacheTTL seconds, self, RefreshCache(WinnerRequest(Some(eventId), 0, None)))

    }

    case RefreshCache(winnerRequest) =>
      log.info(s"EventId: ${winnerRequest.eventId} cleared from cache")
      context.actorOf(Props(new LotteryRequester(AttendeesRequest(self, winnerRequest))), "requestor")

    case RefreshCurrentEventId =>
      import io.circe.generic.auto._
      import de.heikoseeberger.akkahttpcirce.CirceSupport._
      implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
      val http = Http(context.system)
      http.singleRequest(HttpRequest(uri = eventsURI))
        .flatMap(s => Unmarshal(s.entity).to[EventPage])
        .pipeTo(self)

      context.system.scheduler.scheduleOnce(cacheTTL seconds, self, RefreshCurrentEventId)

    case e =>
      log.warning(s"WTF: $e")
  }
}
