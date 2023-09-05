package graboid

import java.time.Duration
import java.time.ZonedDateTime

case class CrawlingScheduling(
    id: String,
    dataCentreId: String,
    starting: Option[ZonedDateTime],
    ending: Option[ZonedDateTime],
    duration: Duration
)
