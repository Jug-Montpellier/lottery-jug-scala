package mthlotto.server

import akka.http.scaladsl.model.{HttpCharsets, HttpEntity, MediaTypes}
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import akka.stream.scaladsl.StreamConverters

trait ResourceHelper {
  val extenstionExtractor = "(?!\\.)[a-z]+$$".r

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
        case _ => None
      }
      inputStream <- Option(getClass.getResourceAsStream(path))
    } yield HttpEntity(contentType = contentType, StreamConverters.fromInputStream(() => inputStream))

    entity
      .map(complete(_))
      .getOrElse(complete(NotFound, "Nope"))
  }
}
