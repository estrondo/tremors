package tremors.graboid

import com.dimafeng.testcontainers.KafkaContainer
import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito.*
import org.testcontainers.utility.DockerImageName
import tremors.quakeml.EventFixture
import tremors.ziotestcontainers.*
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

  private val kafkaContainerLayer = layerOf {
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

        val crawler         = mock(classOf[Crawler])
        val timelineManager = mock(classOf[TimelineManager])

        val beginning = ZonedDateTime.now()
        val ending    = beginning.plusDays(13)
        val window    = TimelineManager.Window("testable", beginning, ending)

        val events = for _ <- 0 until 10 yield EventFixture.createRandom()

        when(timelineManager.nextWindow(meq("testable")))
          .thenReturn(ZIO.succeed(window))

        when(timelineManager.register(meq(window)))
          .thenReturn(ZIO.succeed(window))

        when(crawler.crawl(meq(window)))
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
          detected.size == events.size,
          verify(timelineManager).register(meq(window)) == null
        )
      }
    ).provideLayer(kafkaContainerLayer)
  )
