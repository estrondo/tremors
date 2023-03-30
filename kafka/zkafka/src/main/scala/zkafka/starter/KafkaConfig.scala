package zkafka.starter

import java.time.Duration
import zio.config.magnolia.Descriptor

case class KafkaConfig(
    bootstrap: Seq[String],
    clientId: String,
    closeTimeout: Duration,
    group: Option[String]
)
