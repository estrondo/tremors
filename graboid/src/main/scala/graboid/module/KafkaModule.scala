package graboid.module

import com.softwaremill.macwire.Module
import com.softwaremill.macwire.wire
import tremors.zio.kafka.KafkaConfig
import tremors.zio.kafka.KafkaRouter
import zio.RIO
import zio.Scope

@Module
trait KafkaModule:

  def router: KafkaRouter

object KafkaModule:

  def apply(clientId: String, config: KafkaConfig): RIO[Scope, KafkaModule] =
    for router <- KafkaRouter(clientId, config) yield wire[Impl]

  private class Impl(val router: KafkaRouter) extends KafkaModule
