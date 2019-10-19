package mthlotto.eventbrite

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import mthlotto.model.Attendees

object EventBriteRequester {
  case class AttendeesRequest(eventId: String)
}

class EventBriteRequester(context: ActorContext[EventBriteRequester.AttendeesRequest])
    extends AbstractBehavior[EventBriteRequester.AttendeesRequest](context)
    with EventBriteCapabilities {
  override def onMessage(msg: EventBriteRequester.AttendeesRequest): Behavior[EventBriteRequester.AttendeesRequest] =
    this

  override val actorSystem = context.system.toClassic

}
