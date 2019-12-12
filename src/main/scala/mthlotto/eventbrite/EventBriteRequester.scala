package mthlotto.eventbrite

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import akka.actor.typed.{Behavior, Signal}
import akka.actor.typed.scaladsl.adapter._
import akka.actor.ActorLogging
import akka.actor.TypedActor.PreStart
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import mthlotto.model.Attendees

object EventBriteRequester {
  case class AttendeesRequest(eventId: String)
}

class EventBriteRequester(context: ActorContext[EventBriteRequester.AttendeesRequest])
    extends AbstractBehavior[EventBriteRequester.AttendeesRequest](context)
    with PreStart
    with EventBriteCapabilities {

  override def onMessage(msg: EventBriteRequester.AttendeesRequest): Behavior[EventBriteRequester.AttendeesRequest] =
    this

  override def onSignal: PartialFunction[Signal, Behavior[EventBriteRequester.AttendeesRequest]] = super.onSignal

  override val actorSystem = context.system.toClassic

  override def preStart(): Unit =
    context.log.info("Start...")
}
