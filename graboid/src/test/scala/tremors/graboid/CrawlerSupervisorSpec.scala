package tremors.graboid

import com.dimafeng.testcontainers.KafkaContainer
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import tremors.graboid.DockerLayer.*
import tremors.quakeml.Event
import tremors.quakeml.EventFixture
import tremors.quakeml.ResourceReference
import zio.Console
import zio.Duration
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.Subscription
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import zio.kafka.producer.ProducerSettings.apply
import zio.kafka.serde.Serde
import zio.stream.ZSink
import zio.stream.ZStream
import zio.test.TestEnvironment
import zio.test.assertTrue

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import zio.test.TestClock
import zio.given

object CrawlerSupervisorSpec extends Spec:

  private val kafkaContainer = userContainerLayer {
    KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.2"))
  }

  private val detectedStream = Consumer
    .subscribeAnd(Subscription.topics("seismo-detected"))
    .plainStream(Serde.string, Serde.byteArray)
    .map(_.record)

  private val config = CrawlerSupervisor.Config("testable")

  override def spec = suite("A CrawlerSupervisorSpec")(
    suite("Integration Test with Kafka")(
      test("should publish some events") {

        val crawler         = Mockito.mock(classOf[Crawler])
        val timelineManager = Mockito.mock(classOf[TimelineManager])

        val beginning = ZonedDateTime.now()
        val ending    = beginning.plusDays(13)
        val window    = TimelineManager.Window("testable", beginning, ending)

        val events = for _ <- 0 until 10 yield EventFixture.createRandom()

        Mockito
          .when(timelineManager.nextWindow(ArgumentMatchers.eq("testable")))
          .thenReturn(ZIO.succeed(window))

        Mockito
          .when(crawler.crawl(ArgumentMatchers.eq(window)))
          .thenReturn(ZIO.succeed(ZStream.fromIterable(events)))

        for
          // kafkaPort    <- DockerLayer.getPort("kafka", 29092)
          container <- ZIO.service[KafkaContainer]
          bootstrap  = container.bootstrapServers

          producerLayer =
            ZLayer.scoped(Producer.make(ProducerSettings(List(bootstrap))))

          consumerLayer =
            ZLayer.scoped(
              Consumer.make(
                ConsumerSettings(List(bootstrap))
                  .withGroupId("tester")
                  .withProperty("auto.offset.reset", "earliest")
              )
            )

          crawlerSupervisor = CrawlerSupervisor(config, crawler, producerLayer)
          status           <- crawlerSupervisor.start().provideLayer(ZLayer.succeed(timelineManager))
          fork             <- detectedStream.run(ZSink.collectAllN(10)).provideLayer(consumerLayer).fork
          _                <- TestClock.adjust(5.seconds)
          detected         <- fork.join
        yield assertTrue(
          status.success == events.size.toLong,
          detected.size == events.size
        )
      }
    ).provideLayer(kafkaContainer)
  )
