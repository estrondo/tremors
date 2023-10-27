package toph.config

import tremors.zio.farango.ArangoConfig
import tremors.zio.kafka.KafkaConfig

case class TophConfig(
    arango: ArangoConfig,
    kafka: KafkaConfig,
    grpc: GRPCConfig,
    security: SecurityConfig
)
