package graboid

import com.softwaremill.macwire.wire
import com.softwaremill.macwire.wireWith
import graboid.config.KafkaConfig
import graboid.kafka.GraboidGroup
import graboid.kafka.KafkaManager
import zio.Task
import zio.ZIO
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.producer.ProducerSettings

trait KafkaModule:

  val kafkaManager: KafkaManager

object KafkaModule:

  def apply(config: KafkaConfig): Task[KafkaModule] = ZIO.attempt {
    wire[KafkaModuleImpl]
  }

  private class KafkaModuleImpl(config: KafkaConfig) extends KafkaModule:

    val producerSettings = ProducerSettings(config.bootstrap.toList)
      .withClientId(config.clientId)

    val consumerSettings = ConsumerSettings(config.bootstrap.toList)
      .withCloseTimeout(config.closeTimeout)
      .withGroupId(config.group.getOrElse(GraboidGroup))
      .withClientId(config.clientId)

    override val kafkaManager: KafkaManager = wireWith(KafkaManager.apply)
