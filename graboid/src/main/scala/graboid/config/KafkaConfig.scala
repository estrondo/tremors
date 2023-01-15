package graboid.config

import java.time.Duration

case class KafkaConfig(
    bootstrap: Seq[String],
    clientId: String,
    closeTimeout: Duration,
    group: Option[String]
)
