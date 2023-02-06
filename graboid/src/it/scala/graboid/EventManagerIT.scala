package graboid

import graboid.Crawler.given
import graboid.fixture.CrawlerExecutionFixture
import graboid.fixture.PublisherFixture
import graboid.kafka.GraboidDetectedEvent
import io.bullet.borer.Cbor
import testkit.quakeml.EventFixture
import testkit.quakeml.MagnitudeFixture
import testkit.quakeml.OriginFixture
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.given
import zio.kafka.consumer.Consumer
import zio.kafka.producer.Producer
import zio.stream.ZSink
import zio.test.TestClock
import zio.test.TestEnvironment
import zio.test.assertTrue
import testkit.zio.testcontainers.KafkaLayer

object EventManagerIT extends IT:

  def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A EventManager")(
      suite("with Kafka's container")(
        test("It should send info objects to correct topic.") {
          val expectedEvent     = EventFixture.createRandom()
          val expectedOrigin    = OriginFixture.createRandom()
          val expectedMagniture = MagnitudeFixture.createRandom()
          val publisher         = PublisherFixture.createRandom()
          val execution         = CrawlerExecutionFixture.createRandom()

          for
            manager        <- ZIO.service[EventManager]
            consumerStream <- KafkaLayer.consume(GraboidDetectedEvent)
            _              <- manager.register(expectedEvent, publisher, execution)
            _              <- manager.register(expectedOrigin, publisher, execution)
            _              <- manager.register(expectedMagniture, publisher, execution)
            fiber          <- consumerStream.run(ZSink.take(3)).fork
            _              <- TestClock.adjust(1.second)
            result         <- fiber.join
            restored       <- ZIO.foreach(result)(record => ZIO.attempt(Cbor.decode(record.value()).to[Crawler.Info].value))
          yield
            val map = restored.groupBy(_.getClass()).mapValues(_.head)
            assertTrue(
              map.size == 3,
              map(expectedOrigin.getClass()) == expectedOrigin,
              map(expectedMagniture.getClass()) == expectedMagniture,
              map(expectedEvent.getClass()) == expectedEvent
            )
        }
      ).provideSomeLayer(
        KafkaLayer.layer ++ (KafkaLayer.layer >>> (KafkaLayer.producerLayer ++ KafkaLayer.createConsumerLayer(
          "graboid"
        ) ++ (KafkaLayer.producerLayer >>> EventManagerLayer)))
      )
    )

  private val EventManagerLayer = ZLayer {
    for producer <- ZIO.service[Producer]
    yield EventManager(ZLayer.succeed(producer))
  }
