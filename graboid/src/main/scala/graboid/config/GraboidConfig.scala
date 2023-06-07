package graboid.config

import tremors.zio.kafka.KafkaConfig

final case class GraboidConfig(
    kafka: KafkaConfig
)
