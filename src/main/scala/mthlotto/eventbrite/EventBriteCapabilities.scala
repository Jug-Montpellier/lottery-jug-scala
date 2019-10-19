package mthlotto.eventbrite

import io.circe.generic.auto._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import mthlotto.model.{Attendeed, Attendees}

trait EventBriteCapabilities {
  import mthlotto.LotteryConf._

  protected val eventsURL =
    s"https://www.eventbriteapi.com/v3/events/search/?token=$token&organizer.id=$organizerId"

  def attendeesURL(eventId: String, page: Int) =
    s"https://www.eventbriteapi.com/v3/events/$eventId/attendees/?token=$token&page=$page"

  implicit val actorSystem: ActorSystem

  import actorSystem.dispatcher

  implicit val mat = Materializer

  lazy val http = Http(actorSystem)

  var attendees: List[Attendeed] = List()

  var awaitedPages = Set[Int]()

  def buildAwaitedPages(attendees: Attendees): Set[Int] = (1 to attendees.pagination.page_count).toSet.drop(1)

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  def sendAttendeesRequest(eventId: String, page: Int = 1) =
    http
      .singleRequest(HttpRequest(uri = attendeesURL(eventId, page)))
      .flatMap(s => Unmarshal(s.entity).to[Attendees])
}
