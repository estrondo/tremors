package graboid.config

import tremors.zio.farango.ArangoConfig
import tremors.zio.kafka.KafkaConfig

final case class GraboidConfig(
                                arango: ArangoConfig,
                                kafka: KafkaConfig
)
