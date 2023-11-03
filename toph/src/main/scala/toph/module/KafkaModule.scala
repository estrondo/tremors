package toph.module

import tremors.zio.kafka.KafkaConfig
import tremors.zio.kafka.KafkaRouter
import zio.RIO
import zio.Scope

class KafkaModule(val router: KafkaRouter)

object KafkaModule:

  def apply(config: KafkaConfig): RIO[Scope, KafkaModule] =
    for router <- KafkaRouter("toph", config) yield new KafkaModule(router)
