package zkafka.starter

import java.time.Duration

case class KafkaConfig(
    bootstrap: List[String],
    clientId: String,
    closeTimeout: Duration,
    group: Option[String]
)
