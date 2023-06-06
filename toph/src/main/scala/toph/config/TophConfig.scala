package toph.config

import farango.zio.starter.ArangoConfig
import zkafka.starter.KafkaConfig

case class TophConfig(
    arango: ArangoConfig,
    kafka: KafkaConfig,
    grpc: GRPCConfig
)
