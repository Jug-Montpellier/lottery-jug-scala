package lottery

import java.nio.ByteBuffer

import io.circe.Decoder._
import io.circe._
import io.circe.jawn._
import io.circe.generic.auto._

object EventBriteParser {

  implicit val attendeedDecoder = new Decoder[Attendeed] {
    override def apply(c: HCursor): Result[Attendeed] =
      for {
        profile <- c.get[Attendeed]("profile")
      } yield profile
  }

  def parseEvent(buffer: ByteBuffer): Either[Error, Attendees] =
    parseByteBuffer(buffer)
      .flatMap { json =>
        val c = json.hcursor
        for {
          pagination <- c.get[Pagination]("pagination")
          attendees <- c.get[List[Attendeed]]("attendees")
        } yield Attendees(pagination = pagination, attendees = attendees)
      }

  def parseEventList(buffer: ByteBuffer): Either[Error, List[Event]] =
    parseByteBuffer(buffer)
      .flatMap { json =>
        val c = json.hcursor
        for {
          pagination <- c.get[Pagination]("pagination")
          events <- c.get[List[Event]]("events")
        } yield events
      }

}
