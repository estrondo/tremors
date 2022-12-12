package graboid

import com.dimafeng.testcontainers.KafkaContainer
import org.mockito.Mockito.*
import org.mockito.{ArgumentMatchers => Args}
import testkit.quakeml.EventFixture
import testkit.zio.testcontainers.*
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.durationInt
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.Subscription
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import zio.kafka.serde.Serde
import zio.stream.ZSink
import zio.stream.ZStream
import zio.test.TestClock
import zio.test.assertTrue

import java.time.ZonedDateTime

object CrawlerSupervisorSpec extends Spec:

  private val detectedStream = Consumer
    .subscribeAnd(Subscription.topics("tremors.detected-event"))
    .plainStream(Serde.string, Serde.byteArray)
    .map(_.record)

  private val config = CrawlerSupervisor.Config("testable")

  override def spec: zio.test.Spec[Scope, Any] = suite("A CrawlerSupervisorSpec")(
    suite("Integration Test with Kafka")(
      test("should publish some events") {

        val crawler         = mock(classOf[Crawler])
        val timelineManager = mock(classOf[TimelineManager])

        val beginning = ZonedDateTime.now()
        val ending    = beginning.plusDays(13)
        val window    = TimelineManager.Window("testable-id", beginning, ending)

        val events = for _ <- 0 until 10 yield EventFixture.createRandom()

        when(timelineManager.nextWindow(Args.eq("testable")))
          .thenReturn(ZIO.succeed(window))

        when(timelineManager.register(Args.eq("testable"), Args.eq(window)))
          .thenReturn(ZIO.succeed(window))

        when(crawler.crawl(Args.eq(window)))
          .thenReturn(ZIO.succeed(ZStream.fromIterable(events)))

        for
          producerLayer    <- KafkaContainerLayer.createProducerLayer()
          consumerLayer    <- KafkaContainerLayer.createConsumerLayer("test-group")
          crawlerSupervisor = CrawlerSupervisor(config, crawler, producerLayer)
          status           <- crawlerSupervisor.run().provideLayer(ZLayer.succeed(timelineManager))
          fork             <- detectedStream.run(ZSink.collectAllN(10)).provideLayer(consumerLayer).fork
          _                <- TestClock.adjust(5.seconds)
          detected         <- fork.join
        yield assertTrue(
          status.success == events.size.toLong,
          detected.size == events.size,
          verify(timelineManager).register(Args.eq("testable"), Args.eq(window)) == null
        )
      }
    ).provideSomeLayer(KafkaContainerLayer.layer)
  )