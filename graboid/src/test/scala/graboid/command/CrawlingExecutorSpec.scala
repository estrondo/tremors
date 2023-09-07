package graboid.command

import graboid.CrawlingExecution
import graboid.CrawlingScheduling
import graboid.CrawlingSchedulingFixture
import graboid.DataCentre
import graboid.GraboidSpec
import graboid.crawling.CrawlingExecutor
import graboid.crawling.CrawlingQueryFixture
import graboid.crawling.EventCrawler
import graboid.crawling.EventCrawlingQuery
import graboid.manager.DataCentreFixture
import graboid.repository.CrawlingExecutionRepository
import graboid.time.ZonedDateTimeService
import java.time.ZonedDateTime
import one.estrondo.sweetmockito.Answer
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.Mockito
import tremors.ZonedDateTimeFixture
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.quakeml.EventFixture
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.http.Client
import zio.kafka.producer.Producer
import zio.stream.ZStream
import zio.test.assertTrue

object CrawlingExecutorSpec extends GraboidSpec:

  override def spec = suite("CrawlingExecutorSpec")(
    test("It should store an empty execution") {
      for
        result        <- testEvent(ZStream.empty)
        (execution, _) = result
      yield assertTrue(
        execution.succeed == 0L,
        execution.failed == 0L,
        execution.state == CrawlingExecution.State.Completed
      )
    },
    test("It should successfully execute some events.") {
      for
        result        <- testEvent(ZStream.fromIterable(Seq(EventCrawler.Success(EventFixture.createRandom()))))
        (execution, _) = result
      yield assertTrue(
        execution.succeed == 1,
        execution.state == CrawlingExecution.State.Completed
      )
    },
    test("It should execute with some failures.") {
      for
        result        <- testEvent(
                           ZStream.fromIterable(
                             Seq(
                               EventCrawler.Success(EventFixture.createRandom()),
                               EventCrawler.Failed(EventFixture.createRandom(), new RuntimeException())
                             )
                           )
                         )
        (execution, _) = result
      yield assertTrue(
        execution.succeed == 1,
        execution.failed == 1,
        execution.state == CrawlingExecution.State.Completed
      )
    },
    test("It should mark the the execution as failed.") {
      for
        result        <- testEvent(ZStream.fail(new IllegalStateException("@@@")))
        (execution, _) = result
      yield assertTrue(
        execution.state == CrawlingExecution.State.Failed
      )
    }
  ).provideSome[Scope](
    SweetMockitoLayer.newMockLayer[CrawlingExecutionRepository],
    SweetMockitoLayer.newMockLayer[ZonedDateTimeService],
    SweetMockitoLayer.newMockLayer[Producer],
    SweetMockitoLayer.newMockLayer[Client],
    SweetMockitoLayer.newMockLayer[KeyGenerator],
    SweetMockitoLayer.newMockLayer[EventCrawler.Factory],
    SweetMockitoLayer.newMockLayer[EventCrawler],
    ZLayer {
      for
        repository   <- ZIO.service[CrawlingExecutionRepository]
        factory      <- ZIO.service[EventCrawler.Factory]
        keyGenerator <- ZIO.service[KeyGenerator]
      yield CrawlingExecutor(repository, factory, keyGenerator)
    }
  )

  private def testEvent(stream: ZStream[Any, Throwable, EventCrawler.Result]) =
    test(CrawlingScheduling.Service.Event, stream)

  private def test(service: CrawlingScheduling.Service, stream: ZStream[Any, Throwable, EventCrawler.Result]) =
    val context = Context(service)
    import context.*

    for
      executor     <- ZIO.service[CrawlingExecutor]
      timeService  <- ZIO.service[ZonedDateTimeService]
      keyGenerator <- ZIO.service[KeyGenerator]
      _             = Mockito.when(timeService.now()).thenReturn(zonedDateTime)
      _             = Mockito.when(keyGenerator.generate(KeyLength.Medium)).thenReturn(id)

      crawler <- ZIO.service[EventCrawler]
      factory <- ZIO.service[EventCrawler.Factory]

      _ = Mockito.when(factory(dataCentre)).thenReturn(ZIO.succeed(crawler))
      _ = Mockito.when(crawler(eqTo(query))).thenReturn(stream)

      _ <- SweetMockitoLayer[CrawlingExecutionRepository]
             .whenF2(_.updateState(any()))
             .thenAnswer(invocation => Answer.succeed(invocation.getArgument[CrawlingExecution](0)))

      _ <- SweetMockitoLayer[CrawlingExecutionRepository]
             .whenF2(_.updateCounting(any()))
             .thenAnswer(invocation => Answer.succeed(invocation.getArgument[CrawlingExecution](0)))

      _ <- SweetMockitoLayer[CrawlingExecutionRepository]
             .whenF2(_.insert(execution))
             .thenReturn(execution)

      result <- executor.execute(context.dataCentre, context.query)
    yield (result, context)

  private class Context(val service: CrawlingScheduling.Service) {
    val dataCentre: DataCentre         = DataCentreFixture.createRandom()
    val query: EventCrawlingQuery      = CrawlingQueryFixture.createRandom()
    val zonedDateTime: ZonedDateTime   = ZonedDateTimeFixture.createRandom()
    val id: String                     = KeyGenerator.generate(KeyLength.Medium)
    val scheduling: CrawlingScheduling = CrawlingSchedulingFixture.createRandom(dataCentre, service)

    val execution: CrawlingExecution = CrawlingExecution(
      id = id,
      schedulingId = scheduling.id,
      createdAt = zonedDateTime,
      updatedAt = None,
      succeed = 0L,
      failed = 0L,
      state = CrawlingExecution.State.Starting
    )
  }
