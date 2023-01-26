package graboid

import zio.Scope

import zio.test.TestEnvironment
import zio.ZIO
import zio.ZLayer
import com.softwaremill.macwire.wireWith
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import org.mockito.ArgumentMatchers.{eq => eqTo, any}
import java.time.ZonedDateTime
import one.estrondo.sweetmockito.Answer
import one.estrondo.sweetmockito.zio.given
import graboid.fixture.CrawlerExecutionFixture
import zio.test.TestClock
import zio.test.TestClock.Data
import java.time.Instant
import java.time.Clock
import zio.test.Annotations
import zio.test.Live
import zio.test.assertTrue
import graboid.fixture.PublisherFixture
import one.estrondo.sweetmockito.SweetMockito
import zio.stream.ZStream
import testkit.quakeml.EventFixture
import testkit.core.createZonedDateTime

object CrawlerExecutorSpec extends Spec:

  def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A CrawlerExecutor")(
      test("It should find all expected events from the beginning.") {
        val now        = createZonedDateTime()
        val executions = CrawlerExecutionFixture.createRandomSeq(10)
        val publisher  = PublisherFixture.createRandom()
        val event      = EventFixture.createRandom()
        val crawler    = SweetMockito[Crawler]

        SweetMockito
          .whenF2(crawler.crawl(any()))
          .thenReturn(ZStream.succeed(event))

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
          _        <- SweetMockitoLayer[EventManager]
                        .whenF2(_.register(eqTo(event), eqTo(publisher), any()))
                        .thenReturn(event)
          executor <- ZIO.service[CrawlerExecutor]
          _        <- TestClock.setTime(now.toInstant())
          report   <- executor.run()
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
