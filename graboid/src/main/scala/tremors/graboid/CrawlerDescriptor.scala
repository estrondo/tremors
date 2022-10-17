package tremors.graboid

import java.time.Duration
import java.time.ZonedDateTime

case class CrawlerDescriptor(
    name: String,
    `type`: String,
    source: String,
    windowDuration: Duration,
    starting: ZonedDateTime
)
