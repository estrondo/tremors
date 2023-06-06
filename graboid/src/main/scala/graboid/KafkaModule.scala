package graboid

import com.softwaremill.macwire.wire
import io.bullet.borer.Decoder
import io.bullet.borer.Encoder
import zio.Task
import zio.TaskLayer
import zio.kafka.consumer.Consumer
import zio.kafka.producer.Producer
import zio.stream.ZStream
import zkafka.KafkaManager
import zkafka.KafkaProducer
import zkafka.KafkaSubscriber
import zkafka.starter.KafkaConfig
import zkafka.starter.KafkaManagerStarter

trait KafkaModule:

  def kafkaManager: KafkaManager

  def consumerLayer: TaskLayer[Consumer]

  def producerLayer: TaskLayer[Producer]

  def subscribe[A: Decoder, B, C: Encoder](
      topic: String,
      kafkaSubscriber: KafkaSubscriber[A, B],
      kafkaProducer: KafkaProducer[B, C]
  ): Task[ZStream[Any, Throwable, (B, Seq[C])]]

object KafkaModule:

  def apply(config: KafkaConfig): Task[KafkaModule] =
    for kafkaManager <- KafkaManagerStarter(config, "graboid")
    yield wire[Impl]

  private class Impl(override val kafkaManager: KafkaManager) extends KafkaModule:
    val consumerLayer: TaskLayer[Consumer] = kafkaManager.consumerLayer

    val producerLayer: TaskLayer[Producer] = kafkaManager.producerLayer

    override def subscribe[A: Decoder, B, C: Encoder](
        topic: String,
        kafkaSubscriber: KafkaSubscriber[A, B],
        kafkaProducer: KafkaProducer[B, C]
    ): Task[ZStream[Any, Throwable, (B, Seq[C])]] =
      kafkaManager.subscribe(topic, kafkaSubscriber, kafkaProducer)
