package graboid

import java.time.Duration
import java.time.ZonedDateTime

case class CrawlingScheduling(
    id: String,
    dataCentreId: String,
    service: CrawlingScheduling.Service,
    starting: Option[ZonedDateTime],
    ending: Option[ZonedDateTime],
    duration: Duration
)

object CrawlingScheduling:

  enum Service:
    case Event, Dataselect
