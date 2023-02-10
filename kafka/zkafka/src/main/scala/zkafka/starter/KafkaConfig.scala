package zkafka.starter

import zio.config.magnolia.Descriptor

import java.time.Duration

object KafkaConfig:
  given Descriptor[Seq[String]] = ???

case class KafkaConfig(
    bootstrap: Seq[String],
    clientId: String,
    closeTimeout: Duration,
    group: Option[String]
)
