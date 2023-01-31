package graboid.kafka

import com.softwaremill.macwire.wire
import io.bullet.borer.Cbor
import io.bullet.borer.Decoder
import io.bullet.borer.Encoder
import org.apache.kafka.clients.producer.ProducerRecord
import zio.RIO
import zio.Task
import zio.UIO
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
import zio.TaskLayer

trait KafkaManager:

  def producerLayer: TaskLayer[Producer]

  def consumerLayer: TaskLayer[Consumer]

  def subscribe[A: Decoder, B, C: Encoder](
      topic: String,
      subscriber: KafkaSubscriber[A, B],
      producer: KafkaProducer[B, C]
  ): UIO[ZStream[Any, Throwable, (B, Seq[C])]]

object KafkaManager:

  private type SourceElement[X] = (Offset, String, Option[X])

  private type SourceStream[X] = ZStream[Producer & Consumer, Throwable, SourceElement[X]]

  def apply(consumerSettings: ConsumerSettings, producerSettings: ProducerSettings): KafkaManager =
    wire[Impl]

  private class Impl(
      consumerSettings: ConsumerSettings,
      producerSettings: ProducerSettings
  ) extends KafkaManager:

    val producer = Producer.make(producerSettings)
    val consumer = Consumer.make(consumerSettings)

    val producerLayer = ZLayer.scoped(producer)

    val consumerLayer = ZLayer.scoped(consumer)

    val kafkaLayer = producerLayer ++ consumerLayer

    def subscribe[A: Decoder, B, C: Encoder](
        topic: String,
        subscriber: KafkaSubscriber[A, B],
        producer: KafkaProducer[B, C]
    ): UIO[ZStream[Any, Throwable, (B, Seq[C])]] =
      for
        source <- createSource(topic, subscriber)
        stream <- consumeAndProduce(topic, source, producer)
      yield stream.provideLayer(kafkaLayer)

    def createSource[A: Decoder, B](topic: String, subscriber: KafkaSubscriber[A, B]): UIO[SourceStream[B]] =
      ZIO.succeed {
        Consumer
          .subscribeAnd(Subscription.topics(topic))
          .plainStream(Serde.string, Serde.byteArray)
          .tap(record =>
            ZIO.logDebug(
              s"A new record has just been received: topic=$topic, key=${record.key} and offset=${record.offset.offset}."
            )
          )
          .mapZIO({ record =>
            ZIO.fromTry {
              for decoded <- Cbor.decode(record.value).to[A].valueTry
              yield (record.offset, record.key, decoded)
            }
          })
          .mapZIO((offset, key, value) => subscriber.accept(key, value).map((offset, key, _)))
          .tap((_, _, message) =>
            ZIO.logDebug(
              s"A Subscriber has produced a value: topic=$topic and hasContent=${message.isDefined}."
            )
          )
      }

    def consumeAndProduce[B, C: Encoder](
        topic: String,
        source: SourceStream[B],
        producer: KafkaProducer[B, C]
    ): UIO[ZStream[Producer & Consumer, Throwable, (B, Seq[C])]] = ZIO.succeed {
      ZStream
        .fromZIO(ZIO.logInfo(s"A new Subcriber and Producer have just been added to topic=$topic.")) *> source
        .mapZIO(produceWith(producer))
        .mapZIO((offset, value, produced) => offset.commit as value.map(_ -> produced))
        .collectSome
    }

    def produceWith[B, C: Encoder](producer: KafkaProducer[B, C])(
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

    def produceMessage[C: Encoder](message: KafkaMessage[C]): RIO[Producer, C] =
      for
        record   <- createRecord(message)
        metadata <- Producer.produce(record, Serde.string, Serde.byteArray)
        _        <- ZIO.logDebug(s"A message has just been sent: topic=${metadata.topic()} and offset=${metadata.offset()}.")
      yield message.value

    def createRecord[C: Encoder](message: KafkaMessage[C]): Task[ProducerRecord[String, Array[Byte]]] =
      for bytes <- ZIO.fromTry(Cbor.encode(message.value).toByteArrayTry)
      yield message.key match
        case Some(key) => ProducerRecord(message.topic, key, bytes)
        case None      => ProducerRecord(message.topic, bytes)
