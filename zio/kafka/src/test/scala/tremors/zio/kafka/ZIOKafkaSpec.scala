package tremors.zio.kafka

import com.dimafeng.testcontainers.KafkaContainer
import zio.Runtime
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import zio.logging.backend.SLF4J
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.testEnvironment

abstract class ZIOKafkaSpec extends ZIOSpecDefault:

  override val bootstrap: ZLayer[Any, Any, TestEnvironment] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j >>> testEnvironment

  val kafkaContainer: ZLayer[Scope, Throwable, KafkaContainer] = ZLayer {
    val acquire = ZIO.attemptBlocking {
      val container = KafkaContainer()
      container.start()
      container
    }

    ZIO.acquireRelease(acquire)(container => ZIO.attemptBlocking(container.stop()).orDie)
  }

  val kafkaProducer: ZLayer[Scope & KafkaContainer, Throwable, Producer] = ZLayer {
    for
      container <- ZIO.service[KafkaContainer]
      producer  <- Producer.make(ProducerSettings(List(container.bootstrapServers)))
    yield producer
  }

  val kafkaConsumer = ZLayer {
    for
      container <- ZIO.service[KafkaContainer]
      consumer  <- Consumer.make(ConsumerSettings(List(container.bootstrapServers)).withGroupId("tester"))
    yield consumer
  }
