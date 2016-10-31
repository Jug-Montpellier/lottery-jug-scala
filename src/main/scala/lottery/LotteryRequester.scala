package lottery

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import lottery.LotteryProtocol.AttendeesRequest

import io.circe.generic.auto._


class LotteryRequester(attendeesRequest: AttendeesRequest) extends Actor with ActorLogging {

  import LotteryConf._
  import LotteryProtocol._

  val events = s"https://www.eventbriteapi.com/v3/events/search/?token=$token&organizer.id=$organizerId"

  def attendees(eventId: String, page: Int ) = s"https://www.eventbriteapi.com/v3/events/$eventId/attendees/?token=$token&page=$page"

  val http = Http(context.system)

  import context.dispatcher

  var awaitedPages = Set[Int]()

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  import akka.pattern.pipe

  var attendees: List[Attendeed] = List()

  override def preStart(): Unit = {
    import EventBriteParser._
    import de.heikoseeberger.akkahttpcirce.CirceSupport._

    attendeesRequest.request.eventId.map {
      eventId =>
        http.singleRequest(HttpRequest(uri = attendees(eventId, 1)))
          .flatMap(s => Unmarshal(s.entity).to[Attendees])
          .map {
            a =>
              awaitedPages = (1 to a.pagination.page_count).toSet
              awaitedPages.drop(1)
                .foreach {
                  page =>
                    http.singleRequest(HttpRequest(uri = attendees(eventId, page)))
                      .flatMap(s => Unmarshal(s.entity).to[Attendees])
                      .map(a => (page, a)).pipeTo(self)
                }
              (1, a)
          }
          .pipeTo(self).recover {
          case e =>
            attendeesRequest.sender ! AttenteesResponse(attendeesRequest.request, attendees)
            context.stop(self)
            log.error(e, attendeesRequest.request.toString)
        }
    }


  }

  override def receive: Receive = {

    case (page: Int, a: Attendees) =>
      attendees ++= a.attendees
      awaitedPages -= page

      if (awaitedPages.isEmpty)
        attendeesRequest.sender ! AttenteesResponse(attendeesRequest.request, attendees)

      context.stop(self)
      log.info("Bye bye")
    case e =>
      log.warning(s"WTF $e")

  }
}
