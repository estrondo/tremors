package graboid.crawling

import graboid.GraboidSpec
import graboid.manager.DataCentreFixture
import graboid.time.ZonedDateTimeService
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.Mockito
import tremors.ZonedDateTimeFixture
import tremors.quakeml.EventFixture
import tremors.zio.farango.DataStore
import zio.ZIO
import zio.ZLayer
import zio.http.Client
import zio.kafka.producer.Producer
import zio.stream.ZStream
import zio.test.assertTrue

import java.time.temporal.ChronoUnit

object CrawlingSchedulerSpec extends GraboidSpec:

  def spec = suite("CrawlingSchedulerSpec")(
    suite("It should to schedule FDSN Event Specification.")(
      test("It should send a EventCrawling to the CrawlingExecutor.") {
        val config   = CrawlingSchedulerFixture.EventConfig.createRandom()
        val previous = ZonedDateTimeFixture.createRandom().truncatedTo(ChronoUnit.MINUTES)
        val now      = previous.plusHours(4)

        val expectedQuery = EventCrawlingQuery(
          starting = previous,
          ending = now,
          timeWindow = config.queryWindow,
          owner = EventCrawlingQuery.Owner.Scheduler,
          queries =
            for query <- config.queries
            yield EventCrawlingQuery.Query(
              magnitudeType = query.magnitudeType,
              eventType = query.eventType,
              min = query.minMagnitude,
              max = query.maxMagnitude
            )
        )

        val event       = EventFixture.createRandom()
        val foundEvents = Seq(EventCrawler.FoundEvent(DataCentreFixture.createRandom(), event))

        for
          _        <- SweetMockitoLayer[DataStore]
                        .whenF2(_.get(eqTo(CrawlingScheduler.EventTimeMark))(any()))
                        .thenReturn(Some(previous))
          _        <- SweetMockitoLayer[DataStore]
                        .whenF2(_.put(eqTo(CrawlingScheduler.EventTimeMark), eqTo(now))(any(), any()))
                        .thenReturn(Some(now))
          _        <- ZIO.serviceWith[ZonedDateTimeService](service => Mockito.when(service.now()).thenReturn(now))
          executor <- ZIO.service[CrawlingExecutor]
          _         = Mockito.when(executor.execute(expectedQuery)).thenReturn(ZStream.fromIterable(foundEvents))

          result <- ZIO.serviceWithZIO[CrawlingScheduler](_.start(config).runCollect)
        yield assertTrue(result == Seq(event))
      }
    ).provideSome(
      SweetMockitoLayer.newMockLayer[Client],
      SweetMockitoLayer.newMockLayer[Producer],
      SweetMockitoLayer.newMockLayer[DataStore],
      SweetMockitoLayer.newMockLayer[ZonedDateTimeService],
      SweetMockitoLayer.newMockLayer[CrawlingExecutor],
      ZLayer {
        for
          dataStore        <- ZIO.service[DataStore]
          crawlingExecutor <- ZIO.service[CrawlingExecutor]
          scheduler        <- CrawlingScheduler(dataStore, crawlingExecutor)
        yield scheduler
      }
    )
  )
