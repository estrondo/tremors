package tremors.zio.kafka

import com.softwaremill.macwire
import com.softwaremill.macwire.wire
import zio.Cause
import zio.Exit
import zio.RIO
import zio.Schedule
import zio.Scope
import zio.ULayer
import zio.ZIO
import zio.ZLayer
import zio.durationInt
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.Offset
import zio.kafka.consumer.Subscription
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import zio.kafka.serde.Serde
import zio.stream.ZStream
import zio.stream.ZStreamAspect

trait KafkaRouter:

  def subscribe[A: KReader, B, C: KWriter](
      kConsumer: KConsumer[A, B],
      kProducer: KProducer[B, C]
  ): ZStream[Any, Nothing, C]

  def subscribe[A: KReader, B: KWriter](kConPro: KConPro[A, B]): ZStream[Any, Nothing, B]

  def producerLayer: ULayer[Producer]

  def consumerLayer: ULayer[Consumer]

object KafkaRouter:

  def apply(clientId: String, config: KafkaConfig): RIO[Scope, KafkaRouter] =
    for
      liveProducer <- Producer
                        .make(producerSettings(clientId, config))
                        .tapErrorCause(ZIO.logWarningCause("It was impossible to create a producer!", _))
                        .retry(Schedule.forever && Schedule.spaced(5.seconds))
      liveConsumer <- Consumer
                        .make(consumerSettings(clientId, config))
                        .tapErrorCause(ZIO.logWarningCause("It was impossible to create a consumer!", _))
                        .retry(Schedule.forever && Schedule.spaced(5.seconds))
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

    val consumerLayer: ULayer[Consumer] = ZLayer.succeed(liveConsumer)

    val producerLayer: ULayer[Producer] = ZLayer.succeed(liveProducer)

    override def subscribe[A: KReader, B: KWriter](kConPro: KConPro[A, B]): ZStream[Any, Nothing, B] =
      val mapper    = kConPro.mapperFunction
      val kConsumer = KConsumer[A, (String, A)](topic = kConPro.topic, consumer = (k, v) => ZStream.from(k -> v))
      val kProducer = KProducer.Custom[(String, A), B]((k, v) => mapper(k, v))
      subscribe(kConsumer, kProducer)

    override def subscribe[A: KReader, B, C: KWriter](
        kConsumer: KConsumer[A, B],
        kProducer: KProducer[B, C]
    ): ZStream[Any, Nothing, C] =

      val producerFunction = kProducer.producerFunction

      (ZStream.logInfo(s"Starting subscription.") *>
        Consumer
          .plainStream(Subscription.topics(kConsumer.topic), Serde.string, Serde.byteArray)
          .tap(record => ZIO.logDebug(s"New record: offset= ${record.offset.offset}."))
          .mapZIO { record =>
            for either <-
                summon[KReader[A]](record.value)
                  .tapErrorCause(ZIO.logWarningCause(s"Message reading error: offset=${record.offset.offset}.", _))
                  .either
            yield (record, either)
          }
          .collect { case (record, Right(message)) => (record.key, record.offset, message) }
          .flatMap { (key, offset, message) =>
            kConsumer
              .consumer(key, message)
              .flatMap(producerFunction)
              .mapZIO { (topic, key, message) =>
                for
                  output <- summon[KWriter[C]](message)
                  _      <- Producer.produce(topic, key, output, Serde.string, Serde.byteArray)
                yield message
              }
              .ensuringWith {
                case Exit.Success(_)     => offset.commit.ignoreLogged *> ZIO.logDebug(s"Message has been processed.")
                case Exit.Failure(cause) => ZIO.logWarningCause(s"Message has failed!", cause)
              }
              .provideLayer(producerLayer)
              .catchAll { cause =>
                ZStream.fromZIO(ZIO.logWarningCause(s"Unable to process message!", Cause.die(cause))) *> ZStream.empty
              } @@ ZStreamAspect.annotated("kafkaRouter.offset" -> offset.offset.toString)
          }
          .catchAll { cause =>
            ZStream.fromZIO(
              ZIO.logWarningCause(s"It was impossible to consume messages!", Cause.die(cause))
            ) *> ZStream.empty
          }
          .provideLayer(consumerLayer)) @@ ZStreamAspect.annotated("kafkaRouter.topic" -> kConsumer.topic)
