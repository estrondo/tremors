package graboid.config

import java.time.Duration

case class CrawlingConfig(event: EventCrawlingConfig)

case class EventCrawlingConfig(
    interval: Duration,
    queryWindow: Duration,
    queries: List[EventQueryCrawlingConfig]
)

case class EventQueryCrawlingConfig(
    magnitudeType: Option[String],
    minMagnitude: Option[Double],
    maxMagnitude: Option[Double],
    eventType: Option[String]
)
