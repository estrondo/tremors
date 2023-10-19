package toph.module

import tremors.zio.kafka.KafkaConfig
import tremors.zio.kafka.KafkaRouter
import zio.RIO
import zio.Scope

trait KafkaModule

object KafkaModule:

  def apply(config: KafkaConfig): RIO[Scope, KafkaModule] =
    for router <- KafkaRouter("toph", config)
    yield Impl(router)

  private class Impl(router: KafkaRouter) extends KafkaModule
