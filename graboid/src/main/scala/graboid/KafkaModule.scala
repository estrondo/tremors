package graboid

import com.softwaremill.macwire.wire
import com.softwaremill.macwire.wireWith
import graboid.config.KafkaConfig
import graboid.kafka.GraboidGroup
import graboid.kafka.KafkaManager
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.producer.ProducerSettings
import zio.kafka.producer.Producer
import zio.kafka.consumer.Consumer

trait KafkaModule:

  def kafkaManager: KafkaManager

  def consumerLayer: TaskLayer[Consumer]

  def producerLayer: TaskLayer[Producer]

object KafkaModule:

  def apply(config: KafkaConfig): Task[KafkaModule] = ZIO.attempt {
    wire[Impl]
  }

  private class Impl(config: KafkaConfig) extends KafkaModule:

    val producerSettings = ProducerSettings(config.bootstrap.toList)
      .withClientId(config.clientId)

    val consumerSettings = ConsumerSettings(config.bootstrap.toList)
      .withCloseTimeout(config.closeTimeout)
      .withGroupId(config.group.getOrElse(GraboidGroup))
      .withClientId(config.clientId)

    val kafkaManager: KafkaManager = wireWith(KafkaManager.apply)

    val consumerLayer: TaskLayer[Consumer] = kafkaManager.consumerLayer

    val producerLayer: TaskLayer[Producer] = kafkaManager.producerLayer
