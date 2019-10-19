package lottery

import akka.actor.{Actor, ActorLogging}

import lottery.LotteryProtocol.AttendeesRequest
import mthlotto.eventbrite.EventBriteCapabilities
import mthlotto.model.Attendees

class LotteryRequester(attendeesRequest: AttendeesRequest) extends Actor with EventBriteCapabilities with ActorLogging {

  import LotteryProtocol._

  import context.dispatcher

  val actorSystem = context.system

  import akka.pattern.pipe

  override def preStart(): Unit = {

    val eventId = attendeesRequest.eventId

    sendAttendeesRequest(eventId)
      .map { a =>
        awaitedPages = buildAwaitedPages(a)
        awaitedPages
          .foreach { page =>
            sendAttendeesRequest(eventId, page)
              .map(a => (page, a))
              .pipeTo(self)
          }
        (1, a)
      }
      .pipeTo(self)
      .recover {
        case e =>
          attendeesRequest.sender ! AttendeesResponse(eventId, attendees)
          context.stop(self)

      }
  }

  override def receive: Receive = {

    case (page: Int, a: Attendees) =>
      log.debug(awaitedPages.toString() + " - " + page)
      attendees ++= a.attendees
      awaitedPages -= page
      log.debug(awaitedPages.toString())

      if (awaitedPages.isEmpty) {
        attendeesRequest.sender ! AttendeesResponse(attendeesRequest.eventId, attendees)
        log.debug(attendees.size + " attendees")
        context.stop(self)
      }
    case e =>
      log.warning(s"WTF $e")

  }
}
