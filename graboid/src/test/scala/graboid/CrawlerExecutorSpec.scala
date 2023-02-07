package graboid

import _root_.quakeml.DetectedEvent
import com.softwaremill.macwire.wireWith
import graboid.fixture.CrawlerExecutionFixture
import graboid.fixture.PublisherFixture
import one.estrondo.sweetmockito.Answer
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import testkit.core.createZonedDateTime
import testkit.quakeml.EventFixture
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.stream.ZStream
import zio.test.Annotations
import zio.test.Live
import zio.test.TestClock
import zio.test.TestClock.Data
import zio.test.TestEnvironment
import zio.test.assertTrue

import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime

object CrawlerExecutorSpec extends Spec:

  def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A CrawlerExecutor")(
      test("It should find all expected events from the beginning.") {
        val now        = createZonedDateTime()
        val executions = CrawlerExecutionFixture.createRandomSeq(10)
        val publisher  = PublisherFixture.createRandom()
        val event      = EventFixture.createRandom()
        val crawler    = SweetMockito[Crawler]
        val detected   = DetectedEvent(now, event)

        SweetMockito
          .whenF2(crawler.crawl(any()))
          .thenReturn(ZStream.succeed(detected))

        for
          _        <- SweetMockitoLayer[PublisherManager]
                        .whenF2(_.getActives())
                        .thenReturn(ZStream.succeed(publisher))
          _        <- SweetMockitoLayer[CrawlerExecutionRepository]
                        .whenF2(_.searchLast(publisher))
                        .thenReturn(None)
          _        <- SweetMockitoLayer[CrawlerScheduler]
                        .whenF2(_.computeSchedule(eqTo(publisher), eqTo(now)))
                        .thenReturn(executions.iterator)
          _        <- SweetMockitoLayer[CrawlerFactory]
                        .whenF2(_.apply(eqTo(publisher), any()))
                        .thenReturn(crawler)
          _        <- SweetMockitoLayer[CrawlerExecutionRepository]
                        .whenF2(_.add(any[CrawlerExecution]))
                        .thenAnswer(invocation => Answer.succeed(invocation.getArgument[CrawlerExecution](0)))
          _        <- SweetMockitoLayer[EventManager]
                        .whenF2(_.register(eqTo(detected), eqTo(publisher), any()))
                        .thenReturn(detected)
          _        <- SweetMockitoLayer[CrawlerExecutionRepository]
                        .whenF2(_.update(any[CrawlerExecution]))
                        .thenAnswer(invocation => Answer.succeed(Some(invocation.getArgument[CrawlerExecution](0))))
          executor <- ZIO.service[CrawlerExecutor]
          _        <- TestClock.setTime(now.toInstant())
          report   <- executor.run()
        yield assertTrue(
          report == CrawlingReport(10L, 0L, 0L, 0L)
        )
      },
      test("It should find all expected events from the specific publisher.") {
        val now        = createZonedDateTime()
        val executions = CrawlerExecutionFixture.createRandomSeq(10)
        val publisher  = PublisherFixture.createRandom()
        val event      = EventFixture.createRandom()
        val crawler    = SweetMockito[Crawler]
        val detected   = DetectedEvent(now, event)

        SweetMockito
          .whenF2(crawler.crawl(any()))
          .thenReturn(ZStream.succeed(detected))

        for
          _        <- SweetMockitoLayer[PublisherManager]
                        .whenF2(_.get(publisher.key))
                        .thenReturn(Some(publisher))
          _        <- SweetMockitoLayer[CrawlerExecutionRepository]
                        .whenF2(_.searchLast(publisher))
                        .thenReturn(None)
          _        <- SweetMockitoLayer[CrawlerScheduler]
                        .whenF2(_.computeSchedule(eqTo(publisher), eqTo(now)))
                        .thenReturn(executions.iterator)
          _        <- SweetMockitoLayer[CrawlerFactory]
                        .whenF2(_.apply(eqTo(publisher), any()))
                        .thenReturn(crawler)
          _        <- SweetMockitoLayer[CrawlerExecutionRepository]
                        .whenF2(_.add(any[CrawlerExecution]))
                        .thenAnswer(invocation => Answer.succeed(invocation.getArgument[CrawlerExecution](0)))
          _        <- SweetMockitoLayer[EventManager]
                        .whenF2(_.register(eqTo(detected), eqTo(publisher), any()))
                        .thenReturn(detected)
          _        <- SweetMockitoLayer[CrawlerExecutionRepository]
                        .whenF2(_.update(any[CrawlerExecution]))
                        .thenAnswer(invocation => Answer.succeed(Some(invocation.getArgument[CrawlerExecution](0))))
          executor <- ZIO.service[CrawlerExecutor]
          _        <- TestClock.setTime(now.toInstant())
          report   <- executor.runPublisher(publisher.key)
        yield assertTrue(
          report == CrawlingReport(10L, 0L, 0L, 0L)
        )
      }
    ).provideSome(
      CrawlerExecutorLayer,
      SweetMockitoLayer.newMockLayer[CrawlerExecutionRepository],
      SweetMockitoLayer.newMockLayer[CrawlerScheduler],
      SweetMockitoLayer.newMockLayer[PublisherManager],
      SweetMockitoLayer.newMockLayer[EventManager],
      SweetMockitoLayer.newMockLayer[CrawlerFactory]
    )

  private val CrawlerExecutorLayer = ZLayer {
    for
      repository       <- ZIO.service[CrawlerExecutionRepository]
      scheduler        <- ZIO.service[CrawlerScheduler]
      publisherManager <- ZIO.service[PublisherManager]
      eventManager     <- ZIO.service[EventManager]
      crawlerFactory   <- ZIO.service[CrawlerFactory]
    yield wireWith(CrawlerExecutor.apply)
  }
