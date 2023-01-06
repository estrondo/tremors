package graboid

import java.time.ZonedDateTime
import zio.logging.LogAnnotation

case class EventRecord(
    key: String,
    publisherKey: String,
    message: String,
    eventInstant: ZonedDateTime,
    timeWindowKey: Option[String]
)

object EventRecord:
  val Annotation = LogAnnotation[EventRecord](
    "eventRecord",
    (_, x) => x,
    x => s"key=${x.key}, publisherKey=${x.publisherKey}"
  )
