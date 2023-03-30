package zkafka

import com.softwaremill.macwire.wire
import io.bullet.borer.Cbor
import io.bullet.borer.Decoder
import io.bullet.borer.Encoder
import org.apache.kafka.clients.producer.ProducerRecord
import zio.Cause
import zio.RIO
import zio.Task
import zio.TaskLayer
import zio.UIO
import zio.ZIO
import zio.ZLayer
import zio.kafka.consumer.CommittableRecord
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.Offset
import zio.kafka.consumer.Subscription
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import zio.kafka.serde.Serde
import zio.stream.ZStream

trait KafkaManager:

  def producerLayer: TaskLayer[Producer]

  def consumerLayer: TaskLayer[Consumer]

  def subscribe[A: Decoder, B, C: Encoder](
      topic: String,
      subscriber: KafkaSubscriber[A, B],
      producer: KafkaProducer[B, C]
  ): UIO[ZStream[Any, Throwable, (B, Seq[C])]]

  def publish[A: Encoder](stream: ZStream[Any, Nothing, KafkaMessage[A]]): Task[ZStream[Any, Throwable, A]]

object KafkaManager:

  private type SourceElement[X] = (Offset, String, Option[X])

  private type SourceStream[X] = ZStream[Producer & Consumer, Throwable, SourceElement[X]]

  def apply(consumerSettings: ConsumerSettings, producerSettings: ProducerSettings): KafkaManager =
    wire[Impl]

  private class Impl(consumerSettings: ConsumerSettings, producerSettings: ProducerSettings) extends KafkaManager:

    private val producer = Producer.make(producerSettings)
    private val consumer = Consumer.make(consumerSettings)

    private def processors = math.max(Runtime.getRuntime().availableProcessors() / 2, 1)

    val producerLayer = ZLayer.scoped(producer)

    val consumerLayer = ZLayer.scoped(consumer)

    private val kafkaLayer = producerLayer ++ consumerLayer

    def subscribe[A: Decoder, B, C: Encoder](
        topic: String,
        subscriber: KafkaSubscriber[A, B],
        producer: KafkaProducer[B, C]
    ): UIO[ZStream[Any, Throwable, (B, Seq[C])]] =
      for
        source <- createSource(topic, subscriber)
        stream <- consumeAndProduce(topic, source, producer)
      yield stream.provideLayer(kafkaLayer)

    override def publish[A: Encoder](stream: ZStream[Any, Nothing, KafkaMessage[A]]): Task[ZStream[Any, Throwable, A]] =
      ZIO.attempt {
        stream
          .mapZIO(produceMessage)
          .provideLayer(producerLayer)
      }

    private def createSource[A: Decoder, B](topic: String, subscriber: KafkaSubscriber[A, B]): UIO[SourceStream[B]] =
      ZIO.succeed {
        Consumer
          .plainStream(Subscription.topics(topic), Serde.string, Serde.byteArray)
          .mapZIO(decodeRecord(topic))
          .collectSome
          .mapZIOPar(processors)((offset, key, value) =>
            acceptWith(subscriber, topic, key, value).map((offset, key, _))
          )
      }

    private def acceptWith[A, B](
        subscriber: KafkaSubscriber[A, B],
        topic: String,
        key: String,
        value: A
    ): UIO[Option[B]] =
      subscriber
        .accept(key, value)
        .tap {
          case Some(_) => ZIO.logTrace(s"A subscriber of topic=$topic has produced a message for key=$key.")
          case _       => ZIO.logTrace(s"A subscriber of topic=$topic has not produced a message for key=$key.")
        }
        .catchAll(handleSubscriberError(topic))

    private def consumeAndProduce[B, C: Encoder](
        topic: String,
        source: SourceStream[B],
        producer: KafkaProducer[B, C]
    ): UIO[ZStream[Producer & Consumer, Throwable, (B, Seq[C])]] = ZIO.succeed {
      ZStream
        .fromZIO(
          ZIO.logInfo(s"A new Subcriber has been added to topic=$topic, there is also a Producer connected with it.")
        ) *> source
        .mapZIO(produceWith(producer))
        .mapZIO((offset, value, produced) => offset.commit as value.map(_ -> produced))
        .collectSome
    }

    private def decodeRecord[A: Decoder](
        topic: String
    )(record: CommittableRecord[String, Array[Byte]]): UIO[Option[(Offset, String, A)]] =
      (for decoded <- ZIO.fromTry(Cbor.decode(record.value).to[A].valueTry)
      yield Some((record.offset, record.key, decoded)))
        .tap(_ =>
          ZIO.logTrace(
            s"New record has been received from topic=$topic, key=${record.key} and offset=${record.offset.offset}."
          )
        )
        .catchAll(handleDecodeError(topic, record))

    private def handleDecodeError(topic: String, record: CommittableRecord[String, Array[Byte]])(
        cause: Throwable
    ): UIO[Option[Nothing]] =
      ZIO.logWarningCause(
        s"It was impossible to consume record with key=${record.key} from topic=$topic.",
        Cause.die(cause)
      ) *> ZIO.none

    private def handleSubscriberError(topic: String)(cause: Throwable): UIO[None.type] =
      ZIO.logWarningCause(
        s"An error was ocurred during reading of topic=$topic!",
        Cause.die(cause)
      ) as None

    private def produceWith[B, C: Encoder](producer: KafkaProducer[B, C])(
        source: SourceElement[B]
    ): RIO[Producer, (Offset, Option[B], Seq[C])] =
      source match
        case (offset, key, opt @ Some(value)) =>
          for
            messages <- producer.accept(key, value)
            produced <- ZIO.foreach(messages)(produceMessage)
          yield (offset, opt, produced)

        case (offset, _, _) =>
          ZIO.succeed((offset, None, Nil))

    private def produceMessage[C: Encoder](message: KafkaMessage[C]): RIO[Producer, C] =
      for
        record   <- createRecord(message)
        metadata <- Producer.produce(record, Serde.string, Serde.byteArray)
        _        <- ZIO.logDebug(s"A message has just been sent: topic=${metadata.topic()} and offset=${metadata.offset()}.")
      yield message.value

    private def createRecord[C: Encoder](message: KafkaMessage[C]): Task[ProducerRecord[String, Array[Byte]]] =
      for bytes <- ZIO.fromTry(Cbor.encode(message.value).toByteArrayTry)
      yield message.key match
        case Some(key) => ProducerRecord(message.topic, key, bytes)
        case None      => ProducerRecord(message.topic, bytes)
