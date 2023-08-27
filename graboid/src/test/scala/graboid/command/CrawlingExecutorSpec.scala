package graboid.command

import graboid.Crawler
import graboid.CrawlingExecution
import graboid.DataCentre
import graboid.GraboidSpec
import graboid.crawling.CrawlingExecutor
import graboid.crawling.CrawlingQuery
import graboid.crawling.CrawlingQueryFixture
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
        result        <- test(ZStream.empty)
        (execution, _) = result
      yield assertTrue(
        execution.succeed == 0L,
        execution.failed == 0L,
        execution.state == CrawlingExecution.State.Completed
      )
    },
    test("It should successfully execute some events.") {
      for
        result        <- test(ZStream.fromIterable(Seq(Crawler.Success(EventFixture.createRandom()))))
        (execution, _) = result
      yield assertTrue(
        execution.succeed == 1,
        execution.state == CrawlingExecution.State.Completed
      )
    },
    test("It should execute with some failures.") {
      for
        result        <- test(
                           ZStream.fromIterable(
                             Seq(
                               Crawler.Success(EventFixture.createRandom()),
                               Crawler.Failed(EventFixture.createRandom(), new RuntimeException())
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
        result        <- test(ZStream.fail(new IllegalStateException("@@@")))
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
    SweetMockitoLayer.newMockLayer[Crawler.Factory],
    SweetMockitoLayer.newMockLayer[Crawler],
    ZLayer {
      for
        repository   <- ZIO.service[CrawlingExecutionRepository]
        factory      <- ZIO.service[Crawler.Factory]
        keyGenerator <- ZIO.service[KeyGenerator]
      yield CrawlingExecutor(repository, factory, keyGenerator)
    }
  )

  private def test(stream: ZStream[Any, Throwable, Crawler.Result]) =
    val context = Context()
    import context.*

    for
      executor     <- ZIO.service[CrawlingExecutor]
      timeService  <- ZIO.service[ZonedDateTimeService]
      keyGenerator <- ZIO.service[KeyGenerator]
      _             = Mockito.when(timeService.now()).thenReturn(zonedDateTime)
      _             = Mockito.when(keyGenerator.generate(KeyLength.Medium)).thenReturn(id)

      crawler <- ZIO.service[Crawler]
      factory <- ZIO.service[Crawler.Factory]

      _ = Mockito.when(factory(dataCentre)).thenReturn(ZIO.succeed(crawler))
      _ = Mockito.when(crawler(eqTo(query))(using any())).thenReturn(stream)

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

  private class Context {
    val dataCentre: DataCentre       = DataCentreFixture.createRandom()
    val query: CrawlingQuery         = CrawlingQueryFixture.createRandom()
    val zonedDateTime: ZonedDateTime = ZonedDateTimeFixture.createRandom()
    val id: String                   = KeyGenerator.generate(KeyLength.Medium)

    val execution: CrawlingExecution = CrawlingExecution(
      id = id,
      dataCentreId = dataCentre.id,
      createdAt = zonedDateTime,
      updatedAt = None,
      succeed = 0L,
      failed = 0L,
      state = CrawlingExecution.State.Starting
    )
  }
