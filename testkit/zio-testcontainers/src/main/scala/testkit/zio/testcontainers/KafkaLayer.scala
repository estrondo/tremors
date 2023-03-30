package testkit.zio.testcontainers

import com.dimafeng.testcontainers.KafkaContainer
import com.github.dockerjava.api.model.Task
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.testcontainers.utility.DockerImageName
import zio.RIO
import zio.RLayer
import zio.Scope
import zio.TaskLayer
import zio.UIO
import zio.ZIO
import zio.ZLayer
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.Subscription
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import zio.kafka.serde.Serde
import zio.stream.ZStream

object KafkaLayer:

  val layer = layerOf {
    KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.2"))
  }

  val producerLayer: RLayer[KafkaContainer & Scope, Producer] =
    ZLayer {
      for
        container <- ZIO.service[KafkaContainer]
        producer  <- Producer.make(ProducerSettings(List(container.bootstrapServers)))
      yield producer
    }

  def consume(
      topic: String,
      groupId: String = "test",
      offsetReset: String = "earliest"
  ): UIO[ZStream[KafkaContainer & Scope, Throwable, ConsumerRecord[String, Array[Byte]]]] =
    ZIO.succeed(
      Consumer
        .plainStream(Subscription.topics(topic), Serde.string, Serde.byteArray)
        .map(_.record)
        .provideLayer(createConsumerLayer(groupId, offsetReset))
    )

  def createConsumerLayer(
      groupId: String,
      offsetReset: String = "earliest"
  ): RLayer[KafkaContainer & Scope, Consumer] = ZLayer {
    for
      container <- ZIO.service[KafkaContainer]
      consumer  <- Consumer.make(
                     ConsumerSettings(List(container.bootstrapServers))
                       .withGroupId(groupId)
                       .withProperty("auto.offset.reset", offsetReset)
                   )
    yield consumer
  }

  def send(key: String, bytes: Array[Byte], topic: String): RIO[Producer, RecordMetadata] =
    for
      metadata <- Producer.produce(topic, key, bytes, Serde.string, Serde.byteArray)
      _        <- ZIO.logInfo(s"A message has been sent to topic=${metadata.topic()} with offset=${metadata.offset()}.")
    yield metadata

  def consumerSettings(groupdId: String, offsetReset: String = "earliest"): RIO[KafkaContainer, ConsumerSettings] =
    for container <- ZIO.service[KafkaContainer]
    yield ConsumerSettings(List(container.bootstrapServers))
      .withGroupId(groupdId)
      .withProperty("auto.offset.reset", offsetReset)

  def producerSettings(): RIO[KafkaContainer, ProducerSettings] =
    for container <- ZIO.service[KafkaContainer]
    yield ProducerSettings(List(container.bootstrapServers))
