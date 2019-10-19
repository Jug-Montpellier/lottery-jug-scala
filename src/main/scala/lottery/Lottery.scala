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
import lottery.LotteryHttpServerProtocol.{ClearEvents, EventAttendees, Start}
import mthlotto.model.{Attendeed, EventPage}

object LotteryProtocol {

  case object Stop

  case class WinnerRequest(eventId: Option[String], number: Int)

  case class AttendeesRequest(sender: ActorRef, eventId: String)

  case class AttendeesResponse(eventId: String, attendees: List[Attendeed])

  case object RefreshCache

  case object RefreshCurrentEventId

}

class Lottery(httpServerProps: Props) extends Actor with ActorLogging {

  import mthlotto.LotteryConf._
  import LotteryProtocol._

  private val eventsURI =
    s"https://www.eventbriteapi.com/v3/events/search/?organizer.id=$organizerId&token=$token"

  private var currentEventId: Option[String] = None

  private var cache = Map[String, List[Attendeed]]()

  private val httpServer = context.actorOf(httpServerProps, "http-server")

  override def preStart(): Unit = {
    super.preStart()

    httpServer ! Start

    context.system.scheduler.schedule(5 seconds, cacheTTL seconds, self, RefreshCurrentEventId)

  }

  def take(n: Int, attentees: List[Attendeed]) = shuffle(attentees).take(n)

  override def receive: Receive = {

    case EventPage(pagination, events) =>
      if (events.size == 0) {
        currentEventId = None
        log.warning("No opened event!")
        httpServer ! ClearEvents
      } else {
        currentEventId = Some(events(0).id)
        if (events.size > 1)
          log.warning(s"More than one event is open !\n choosing ${events(0)}")
        else
          log.debug(s"Current event ${events(0)}")
        self ! RefreshCache

      }

    case AttendeesResponse(eventId, attendees) =>
      httpServer ! EventAttendees(eventId, attendees)

    case RefreshCache =>
      currentEventId.foreach { eventId =>
        log.debug(s"EventId cleared from cache")
        context.actorOf(
          Props(new LotteryRequester(AttendeesRequest(self, eventId))),
          "requestor"
        )

      }

    case RefreshCurrentEventId =>
      import io.circe.generic.auto._
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      implicit val materializer: ActorMaterializer = ActorMaterializer(
        ActorMaterializerSettings(context.system)
      )
      val http = Http(context.system)
      http
        .singleRequest(HttpRequest(uri = eventsURI))
        .flatMap(s => Unmarshal(s.entity).to[EventPage])
        .pipeTo(self)

    case Stop =>
      httpServer ! Stop

    case e =>
      log.warning(s"WTF: $e")
  }
}
