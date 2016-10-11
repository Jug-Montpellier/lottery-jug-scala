package lottery

import cats.data.Xor.Right
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
      EventBriteParser.parse("/attendees.json") match {
	case Right(att) =>
          att.attendees.size  shouldEqual 6
	case _ => fail()
      }

    }
  }

}
