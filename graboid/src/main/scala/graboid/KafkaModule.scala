package graboid

import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.kafka.producer.Producer
import com.softwaremill.macwire.wire
import graboid.config.KafkaConfig
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.Subscription
import zio.kafka.serde.Serde

trait KafkaModule:

  def consume(topic: String, group: String, fn: (String, Array[Byte]) => Task[Any]): Unit

object KafkaModule:

  def apply(config: KafkaConfig): Task[KafkaModule] = ZIO.attempt {
    wire[KafkaModuleImpl]
  }

  private[graboid] class KafkaModuleImpl(config: KafkaConfig) extends KafkaModule:

    private val consumerSettings = ConsumerSettings(config.bootstrap.toList)
      .withCloseTimeout(config.closeTimeout)
      .withGroupId(config.group.getOrElse("graboid"))
      .withClientId(config.clientId)

    override def consume(
        topic: String,
        group: String,
        fn: (String, Array[Byte]) => Task[Any]
    ): Unit = ??? // TODO: Implement it.
