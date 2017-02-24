package lottery

import akka.actor.{ Actor, ActorLogging }
import akka.dispatch.Futures
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ StandardRoute, _ }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import ch.megard.akka.http.cors.CorsDirectives._
import lottery.LotteryHttpServerProtocol.{
  ClearEvents,
  EventAttendees,
  Shuffle,
  Start
}
import de.heikoseeberger.akkahttpcirce.CirceSupport._
import io.circe.generic.auto._
import io.circe.syntax._
import lottery.LotteryProtocol.Stop

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Random.shuffle

/**
  * Created by chelebithil on 14/11/2016.
  */
object LotteryHttpServerProtocol {

  case object Start

  case object ClearEvents

  case class EventAttendees(eventId: String, attendees: List[Attendeed])

  case object Shuffle

}

class LotteryHttpServer extends Actor with ActorLogging {
  implicit val system           = context.system
  implicit val materializer     = ActorMaterializer()
  implicit val executionContext = context.dispatcher

  var `1` = jsonUTF8("[]")
  var `2` = jsonUTF8("[]")
  var `3` = jsonUTF8("[]")
  var `4` = jsonUTF8("[]")
  var `5` = jsonUTF8("[]")

  var events = Map[String, EventDescription]()

  var attendees = List[Attendeed]()

  val route = cors() {
    path("winners") {
      get {
        parameters('nb.as[Int]) { (n: Int) =>
          val resp = n match {
            case 0 =>
              jsonUTF8("[]")

            case 1 => `1`
            case 2 => `2`
            case 3 => `3`
            case 4 => `4`
            case 5 => `5`

            case i if i > 5 => jsonUTF8(attendees.take(i).asJson.noSpaces)

            case _ =>
              complete(HttpResponse(status = StatusCodes.BadRequest))
          }
          self ! Shuffle
          resp
        }
      } ~ get {
        complete(HttpResponse(status = StatusCodes.BadRequest))
      }
    } ~
    path("") {
      get {
        streamFromClasspath("/public/index.html")
      }
    } ~ path("events") {
      get {
        completeWith(implicitly[ToResponseMarshaller[List[EventDescription]]]) {
          cb =>
            cb(events.values.toList)
        }
      }
    } ~ path("diagram" / Remaining) { fileName =>
      get {
        streamFromClasspath(s"/diagram/$fileName")
      }
    }
  }

  var bindingFutureOption: Option[Future[Http.ServerBinding]] = None

  def prepareNewResponse(attendees: List[Attendeed]) = {

    def json(n: Int) = jsonUTF8(attendees.take(n).asJson.noSpaces)

    `1` = json(1)
    `2` = json(2)
    `3` = json(3)
    `4` = json(4)
    `5` = json(5)
  }

  override def receive = {

    case Stop =>
      bindingFutureOption.foreach { bindingFuture =>
        bindingFuture
          .flatMap(_.unbind()) // trigger unbinding from the port
          .onComplete(_ => system.terminate()) // and shutdown when done
      }

    case Start =>
      bindingFutureOption = Some(Http().bindAndHandle(route, "0.0.0.0", 8080))

      println(
        s"Server online at http://localhost:8080/\nPress Ctrl-C or send SIGTERM to stop..."
      )

      bindingFutureOption.foreach { bindingFuture =>
        sys.addShutdownHook({
          println("Graceful stop")
          bindingFuture
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => system.terminate()) // and shutdown when done
        })
      }

    case EventAttendees(eventId, attendees) =>
      if (attendees != this.attendees)
        log.info(attendees.size + " attendees")
      events = events + (eventId -> EventDescription(eventId, attendees.size))

      prepareNewResponse(shuffle(attendees))

      this.attendees = attendees

    case ClearEvents =>
      events = Map()
      attendees = List()

    case Shuffle =>
      attendees = shuffle(attendees)
      prepareNewResponse(attendees)

    case e =>
      log.warning(s"WTF $e")
  }

  val extenstionExtractor = "(?!\\.)[a-z]+$$".r

  implicit def htmlUTF8(content: String): StandardRoute =
    complete(
      HttpResponse(
        entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, content)
      )
    )

  def jsonUTF8(content: String): StandardRoute =
    complete(
      HttpResponse(
        entity = HttpEntity(ContentTypes.`application/json`, content)
      )
    )

  /**
    * Complete the request by streaming a classpath content.
    *
    * @param path
    * @return
    */
  def streamFromClasspath(path: String): StandardRoute = {
    val entity = for {
      ext <- extenstionExtractor.findFirstIn(path)
      contentType <- ext match {
        case "html" =>
          Some(MediaTypes.`text/html`.withCharset(HttpCharsets.`UTF-8`))
        case "png" => Some(MediaTypes.`image/png`.toContentType)
        case _     => None
      }
      inputStream <- Option(getClass.getResourceAsStream(path))
    } yield
      HttpEntity(contentType = contentType,
                 StreamConverters.fromInputStream(() => inputStream))

    entity
      .map(complete(_))
      .getOrElse(complete(NotFound, "Nope"))
  }

}
