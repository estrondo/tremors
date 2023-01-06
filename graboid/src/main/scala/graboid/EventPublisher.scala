package graboid

import java.net.URL
import java.time.ZonedDateTime

case class EventPublisher(
    key: String,
    name: String,
    url: URL,
    beginning: ZonedDateTime,
    ending: Option[ZonedDateTime],
    `type`: Crawler.Type
)

object EventPublisher:

  case class Invalid(publisher: EventPublisher, cause: Seq[Cause])

  case class Cause(reason: String)
