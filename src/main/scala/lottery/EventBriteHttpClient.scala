package lottery

import scala.util.Random.shuffle

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import de.heikoseeberger.akkahttpcirce.CirceSupport._
import io.circe.generic.auto._

class EventBriteHttpClient(cb: List[Attendeed] => Unit) extends Actor with ActorLogging {

  import LotteryConf._

  val events = s"https://www.eventbriteapi.com/v3/events/search/?token=$token&organizer.id=$organizerId"

  def attendees(eventId: String) = s"https://www.eventbriteapi.com/v3/events/$eventId/attendees/?token=$token"

  val http = Http(context.system)

  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  override def receive: Receive = {
    case eventId: String =>
      import akka.pattern.pipe

      import EventBriteParser._

      http.singleRequest(HttpRequest(uri = attendees(eventId)))
        .flatMap(s => Unmarshal(s.entity).to[Attendees])
        .pipeTo(self)

    case p: Attendees =>
      cb(shuffle(p.attendees).take(3))
      context.stop(self)
      log.info("Bye bye")
    case e =>
      log.warning(s"WTF $e")

  }
}
