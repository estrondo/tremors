package graboid

import _root_.quakeml.QuakeMLDetectedEvent
import cbor.quakeml.given
import graboid.Crawler.given
import graboid.fixture.CrawlerExecutionFixture
import graboid.fixture.PublisherFixture
import graboid.kafka.GraboidDetectedEvent
import io.bullet.borer.Cbor
import testkit.core.createZonedDateTime
import testkit.quakeml.QuakeMLEventFixture
import testkit.quakeml.QuakeMLMagnitudeFixture
import testkit.quakeml.QuakeMLOriginFixture
import testkit.zio.testcontainers.KafkaLayer
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.given
import zio.kafka.consumer.Consumer
import zio.kafka.producer.Producer
import zio.stream.ZSink
import zio.test.TestAspect
import zio.test.TestClock
import zio.test.TestEnvironment
import zio.test.assertTrue

object EventManagerIT extends IT:

  def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A EventManager")(
      suite("with Kafka's container")(
        test("It should send info objects to correct topic.") {
          val now              = createZonedDateTime()
          val expectedEvent    = QuakeMLEventFixture.createRandom()
          val publisher        = PublisherFixture.createRandom()
          val execution        = CrawlerExecutionFixture.createRandom()
          val expectedDetected = QuakeMLDetectedEvent(now, expectedEvent)

          for
            manager        <- ZIO.service[EventManager]
            consumerStream <- KafkaLayer.consume(GraboidDetectedEvent)
            _              <- manager.register(expectedDetected, publisher, execution)
            fiber          <- consumerStream.run(ZSink.head).fork
            _              <- TestClock.adjust(1.second)
            result         <- fiber.join
            restored       <-
              ZIO.foreach(result)(record => ZIO.attempt(Cbor.decode(record.value()).to[QuakeMLDetectedEvent].value))
          yield assertTrue(
            restored == Some(expectedDetected)
          )
        }
      ).provideSomeLayer(
        KafkaLayer.layer ++ (KafkaLayer.layer >>> (KafkaLayer.producerLayer ++ KafkaLayer.createConsumerLayer(
          "graboid"
        ) ++ (KafkaLayer.producerLayer >>> EventManagerLayer)))
      ) @@ TestAspect.sequential
    )

  private val EventManagerLayer = ZLayer {
    for producer <- ZIO.service[Producer]
    yield EventManager(ZLayer.succeed(producer))
  }
