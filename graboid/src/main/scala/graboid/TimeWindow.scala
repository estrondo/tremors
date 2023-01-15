package graboid

import java.time.ZonedDateTime
import zio.logging.LogAnnotation

object TimeWindow:

  val Annotation = LogAnnotation[TimeWindow](
    "TimeWindow",
    (_, x) => x,
    x => s"key=${x.key}, publisherKey=${x.publisherKey}, beginning=${x.beginning}, ending=${x.ending}"
  )

case class TimeWindow(
    key: String,
    publisherKey: String,
    beginning: ZonedDateTime,
    ending: ZonedDateTime,
    successes: Long,
    failures: Long
)
