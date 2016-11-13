package lottery

import java.nio.ByteBuffer

import cats.data.Xor
import io.circe.Decoder._
import io.circe._
import io.circe.jawn._
import io.circe.generic.auto._

object EventBriteParser {

  implicit val attendeedDecoder = new Decoder[Attendeed] {
    override def apply(c: HCursor): Result[Attendeed] = for {
      profile <- c.get[Attendeed]("profile")
    } yield profile
  }

  def parseEvent(buffer: ByteBuffer): Xor[Error, Attendees] = parseByteBuffer(buffer)
    .flatMap {
      json =>
        val c = json.cursor
        for {
          pagination <- c.get[Pagination]("pagination")
          attendees <- c.get[List[Attendeed]]("attendees")
        } yield Attendees(pagination = pagination, attendees = attendees)
    }

  def parseEventList(buffer: ByteBuffer): Xor[Error, List[Event]] = parseByteBuffer(buffer)
    .flatMap {
      json =>
        val c = json.cursor
        for {
          pagination <- c.get[Pagination]("pagination")
          events <- c.get[List[Event]]("events")
        } yield events
    }


}
