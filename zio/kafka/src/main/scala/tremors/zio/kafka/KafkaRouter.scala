package tremors.zio.kafka

import com.softwaremill.macwire
import com.softwaremill.macwire.wire
import zio.Cause
import zio.Exit
import zio.RIO
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.Offset
import zio.kafka.consumer.Subscription
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import zio.kafka.serde.Serde
import zio.stream.ZStream

trait KafkaRouter:

  def subscribe[A: KReader, B, C: KWriter](
      consumer: KConsumer[A, B],
      producer: KProducer[B, C]
  ): ZStream[Any, Nothing, C]

object KafkaRouter:

  def apply(clientId: String, config: KafkaConfig): RIO[Scope, KafkaRouter] =
    for
      liveProducer <- Producer.make(producerSettings(clientId, config))
      liveConsumer <- Consumer.make(consumerSettings(clientId, config))
    yield wire[Impl]

  private def consumerSettings(clientId: String, config: KafkaConfig): ConsumerSettings =
    ConsumerSettings(bootstrapServers = config.consumer.bootstrapServers)
      .withClientId(clientId)
      .withProperties("auto.offset.reset" -> "earliest")
      .withGroupId(config.consumer.groupId)

  private def producerSettings(clientId: String, config: KafkaConfig): ProducerSettings =
    ProducerSettings(bootstrapServers = config.producer.bootstrapServers)
      .withClientId(clientId)

  private class Impl(
      liveProducer: Producer,
      liveConsumer: Consumer
  ) extends KafkaRouter:

    private val consumerLayer = ZLayer.succeed(liveConsumer)

    private val producerLayer = ZLayer.succeed(liveProducer)

    override def subscribe[A: KReader, B, C: KWriter](
        consumer: KConsumer[A, B],
        producer: KProducer[B, C]
    ): ZStream[Any, Nothing, C] =
      val producerFunction = producer.producerFunction
      ZStream.logInfo(s"Subscribing to topic: ${consumer.topic}.") *>
        Consumer
          .plainStream(Subscription.topics(consumer.topic), Serde.string, Serde.byteArray)
          .tap(record => ZIO.logDebug(s"Topic ${consumer.topic}, offset= ${record.offset.offset}."))
          .mapZIO { record =>
            for value <- summon[KReader[A]](record.value)
            yield (consumer.consumer(record.key, value), record.offset)
          }
          .flatMapPar(parallelism) { (stream, offset) =>
            stream
              .flatMap(producerFunction)
              .mapZIO { (topic, key, message) =>
                for
                  output <- summon[KWriter[C]](message)
                  _      <- Producer.produce(topic, key, output, Serde.string, Serde.byteArray)
                yield message
              }
              .ensuringWith {
                case Exit.Success(_)     => offset.commit.orDie
                case Exit.Failure(cause) =>
                  ZIO.logWarningCause(
                    s"An error occurred during Kafka Router producing: subscribing-topic=${consumer.topic}!",
                    cause
                  )

              }
              .provideLayer(producerLayer)
          }
          .catchAll { cause =>
            ZStream
              .fromZIO(
                ZIO.logWarningCause(
                  s"An error occurred during Kafka Router consuming: subscribing-topic=${consumer.topic}!",
                  Cause.die(cause)
                )
              )
              *> ZStream.empty
          }
          .provideLayer(consumerLayer)

    private def parallelism = Runtime.getRuntime.availableProcessors()
