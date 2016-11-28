package lottery

import java.nio.ByteBuffer
import java.nio.file.{Files, Paths}
import org.scalatest.Matchers


import org.scalatest.WordSpec


import scala.language.implicitConversions


class AttendeesParseTestSuite extends WordSpec with Matchers {
  implicit def readBytes(res: String): ByteBuffer =
    ByteBuffer.wrap(Files.readAllBytes(Paths.get(getClass.getResource(res).toURI)))


  "Parser " must {
    "parse attendees" in {
      EventBriteParser.parseEvent("/attendees.json") match {
        case Right(att) =>
          att.attendees.size shouldEqual 6
        case _ => fail()
      }

    }

    "Parse event list" in {
      EventBriteParser.parseEventList("/events.json") match {
        case Right(s) =>
        case Left(e) => fail(e.getMessage)
      }
    }
  }

}
