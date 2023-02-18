package webapi.config

import farango.zio.starter.ArangoConfig
import zkafka.starter.KafkaConfig

case class WebAPIConfig(
    arango: ArangoConfig,
    http: HttpConfig,
    kafka: KafkaConfig
)
