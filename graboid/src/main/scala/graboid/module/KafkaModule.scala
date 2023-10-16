package graboid.module

import tremors.zio.kafka.KafkaConfig
import tremors.zio.kafka.KafkaRouter
import zio.RIO
import zio.Scope
import zio.ULayer
import zio.kafka.consumer.Consumer
import zio.kafka.producer.Producer

trait KafkaModule:

  def router: KafkaRouter

  def consumerLayer: ULayer[Consumer]

  def producerLayer: ULayer[Producer]

object KafkaModule:

  def apply(clientId: String, config: KafkaConfig): RIO[Scope, KafkaModule] =
    for router <- KafkaRouter(clientId, config)
    yield Impl(router)

  private class Impl(val router: KafkaRouter) extends KafkaModule:

    override def consumerLayer: ULayer[Consumer] = router.consumerLayer

    override def producerLayer: ULayer[Producer] = router.producerLayer
