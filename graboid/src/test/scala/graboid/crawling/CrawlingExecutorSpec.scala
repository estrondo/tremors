package graboid.crawling

import graboid.CrawlingExecution
import graboid.CrawlingExecutionFixture
import graboid.GraboidException
import graboid.GraboidSpec
import graboid.manager.DataCentreFixture
import graboid.manager.DataCentreManager
import graboid.repository.CrawlingExecutionRepository
import graboid.time.ZonedDateTimeService
import one.estrondo.sweetmockito.Answer
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.Mockito
import tremors.generator.KeyGenerator
import tremors.quakeml.EventFixture
import zio.ZIO
import zio.ZLayer
import zio.http.Client
import zio.kafka.producer.Producer
import zio.stream.ZStream
import zio.test.Assertion
import zio.test.assert
import zio.test.assertTrue

object CrawlingExecutorSpec extends GraboidSpec:

  private val crawlingExecutionRepositoryInsertExecution = ZIO.serviceWith[CrawlingExecutionRepository] { repository =>
    SweetMockito.whenF2(repository.insert(any())).thenAnswer { invocation =>
      Answer.succeed(invocation.getArgument(0))
    }
  }

  private val crawlingExecutionRepositoryUpdateState = ZIO.serviceWith[CrawlingExecutionRepository] { repository =>
    SweetMockito.whenF2(repository.updateState(any())).thenAnswer { invocation =>
      Answer.succeed(invocation.getArgument[CrawlingExecution](0))
    }
  }

  private val crawlingExecutionRepositoryUpdateCounting = ZIO.serviceWith[CrawlingExecutionRepository] { repository =>
    SweetMockito.whenF2(repository.updateCounting(any())).thenAnswer { invocation =>
      Answer.succeed(invocation.getArgument[CrawlingExecution](0))
    }
  }

  private val crawlingExecutionRepository =
    crawlingExecutionRepositoryUpdateState *> crawlingExecutionRepositoryUpdateCounting *> crawlingExecutionRepositoryInsertExecution

  def spec = suite("CrawlingExecutorSpec")(
    test("It should execute a EventCrawling for all DataCentres.") {
      val dataCentre         = DataCentreFixture.createRandom()
      val query              = EventCrawlingQueryFixture.createRandom(EventCrawlingQuery.Owner.Scheduler)
      val expectedEvent      = EventFixture.createRandom()
      val expectedFoundEvent = EventCrawler.FoundEvent(dataCentre, expectedEvent)

      for
        crawler <- ZIO.service[EventCrawler]
        _       <- SweetMockitoLayer[CrawlingExecutionRepository]
                     .whenF2(_.searchIntersection(dataCentre.id, query.starting, query.ending))
                     .thenReturn(Seq.empty)
        _        = Mockito
                     .when(crawler(query))
                     .thenReturn(ZStream.from(expectedFoundEvent))
        _       <-
          ZIO.serviceWith[DataCentreManager](manager => Mockito.when(manager.all).thenReturn(ZStream.from(dataCentre)))
        _       <- ZIO.serviceWith[EventCrawler.Factory](factory =>
                     Mockito.when(factory(dataCentre)).thenReturn(ZIO.succeed(crawler))
                   )
        _       <- crawlingExecutionRepository
        result  <- ZIO.serviceWithZIO[CrawlingExecutor](_.execute(query).runCollect)
      yield assertTrue(result == Seq(expectedFoundEvent))
    },
    test("It should execute a EventCrawling for a DataCentre.") {

      val dataCentre         = DataCentreFixture.createRandom()
      val query              = EventCrawlingQueryFixture.createRandom(EventCrawlingQuery.Owner.Scheduler)
      val expectedEvent      = EventFixture.createRandom()
      val expectedFoundEvent = EventCrawler.FoundEvent(dataCentre, expectedEvent)

      for
        crawler <- ZIO.service[EventCrawler]
        _       <- SweetMockitoLayer[CrawlingExecutionRepository]
                     .whenF2(_.searchIntersection(dataCentre.id, query.starting, query.ending))
                     .thenReturn(Seq.empty)
        _        = Mockito
                     .when(crawler(query))
                     .thenReturn(ZStream.from(expectedFoundEvent))
        _       <- ZIO.serviceWith[EventCrawler.Factory](factory =>
                     Mockito.when(factory(dataCentre)).thenReturn(ZIO.succeed(crawler))
                   )
        _       <- crawlingExecutionRepository
        result  <- ZIO.serviceWithZIO[CrawlingExecutor](_.execute(dataCentre, query).runCollect)
      yield assertTrue(result == Seq(expectedFoundEvent))
    },
    test("It should fail when there are any intersection with others executions.") {
      val dataCentre             = DataCentreFixture.createRandom()
      val query                  = EventCrawlingQueryFixture.createRandom(EventCrawlingQuery.Owner.Scheduler)
      val expectedEvent          = EventFixture.createRandom()
      val expectedFoundEvent     = EventCrawler.FoundEvent(dataCentre, expectedEvent)
      val otherCrawlingExecution = CrawlingExecutionFixture.createNew()

      for
        crawler <- ZIO.service[EventCrawler]
        _       <- SweetMockitoLayer[CrawlingExecutionRepository]
                     .whenF2(_.searchIntersection(dataCentre.id, query.starting, query.ending))
                     .thenReturn(Seq(otherCrawlingExecution))
        _        = Mockito
                     .when(crawler(query))
                     .thenReturn(ZStream.from(expectedFoundEvent))
        _       <- ZIO.serviceWith[EventCrawler.Factory](factory =>
                     Mockito.when(factory(dataCentre)).thenReturn(ZIO.succeed(crawler))
                   )
        _       <- crawlingExecutionRepository
        exit    <- ZIO.serviceWithZIO[CrawlingExecutor](_.execute(dataCentre, query).runCollect).exit
      yield assert(exit)(Assertion.failsWithA[GraboidException.CrawlingException])
    },
    test("It should crawl when it is a command-crawling.") {
      val dataCentre         = DataCentreFixture.createRandom()
      val query              = EventCrawlingQueryFixture.createRandom(EventCrawlingQuery.Owner.Command)
      val expectedEvent      = EventFixture.createRandom()
      val expectedFoundEvent = EventCrawler.FoundEvent(dataCentre, expectedEvent)

      for
        crawler   <- ZIO.service[EventCrawler]
        _          = Mockito
                       .when(crawler(query))
                       .thenReturn(ZStream.from(expectedFoundEvent))
        _         <- ZIO.serviceWith[EventCrawler.Factory](factory =>
                       Mockito.when(factory(dataCentre)).thenReturn(ZIO.succeed(crawler))
                     )
        _         <- crawlingExecutionRepository
        collected <- ZIO.serviceWithZIO[CrawlingExecutor](_.execute(dataCentre, query).runCollect)
      yield assertTrue(
        collected == Seq(expectedFoundEvent)
      )
    }
  ).provideSome(
    SweetMockitoLayer.newMockLayer[EventCrawler],
    SweetMockitoLayer.newMockLayer[Producer],
    SweetMockitoLayer.newMockLayer[Client],
    SweetMockitoLayer.newMockLayer[CrawlingExecutionRepository],
    SweetMockitoLayer.newMockLayer[DataCentreManager],
    SweetMockitoLayer.newMockLayer[EventCrawler.Factory],
    SweetMockitoLayer.newMockLayer[KeyGenerator],
    SweetMockitoLayer.newMockLayer[ZonedDateTimeService],
    ZLayer {
      for
        repository           <- ZIO.service[CrawlingExecutionRepository]
        manager              <- ZIO.service[DataCentreManager]
        factory              <- ZIO.service[EventCrawler.Factory]
        generator            <- ZIO.service[KeyGenerator]
        zonedDateTimeService <- ZIO.service[ZonedDateTimeService]
      yield CrawlingExecutor(repository, manager, factory, generator, zonedDateTimeService)
    }
  )
