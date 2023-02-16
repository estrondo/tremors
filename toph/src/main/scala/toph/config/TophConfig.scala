package toph.config

import farango.zio.starter.ArangoConfig
import toph.module.GRPCModule
import zkafka.starter.KafkaConfig

case class TophConfig(
    arango: ArangoConfig,
    kafka: KafkaConfig,
    grpc: GRPCConfig
)
