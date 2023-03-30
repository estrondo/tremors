package graboid

import graboid.command.RunAllPublishersExecutor
import graboid.command.RunAllPublishersExecutorImpl
import graboid.protocol.GraboidCommandResult
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import testkit.graboid.protocol.RunAllPublishersFixture
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.assertTrue

object RunAllPublishersExecutorSpec extends Spec:

  def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A RunAllPublishersExecutor")(
      test("It should run and report the success.") {
        val runAll = RunAllPublishersFixture.createRandom()
        for
          _        <- SweetMockitoLayer[CrawlerExecutor]
                        .whenF2(_.run())
                        .thenReturn(CrawlingReport(1, 2, 3, 4))
          executor <- ZIO.service[RunAllPublishersExecutor]
          result   <- executor(runAll)
        yield assertTrue(
          result == GraboidCommandResult(
            id = runAll.id,
            time = result.time,
            status = GraboidCommandResult.ok(
              "All publisher have been run.",
              "events"     -> "1",
              "origins"    -> "3",
              "magnitudes" -> "2",
              "failures"   -> "4"
            )
          )
        )
      },
      test("It should report any failure.") {
        val runAll = RunAllPublishersFixture.createRandom()
        val cause  = IllegalStateException("@@@")
        for
          _        <- SweetMockitoLayer[CrawlerExecutor]
                        .whenF2(_.run())
                        .thenFail(cause)
          executor <- ZIO.service[RunAllPublishersExecutor]
          result   <- executor(runAll)
        yield assertTrue(
          result == GraboidCommandResult(
            id = runAll.id,
            time = result.time,
            status = GraboidCommandResult.failed("It was impossible to run all publishers.", Seq("@@@"))
          )
        )
      }
    ).provideSome(
      crawlerExecutorLayer,
      runAllPublishersExecutorLayer
    )

  private val crawlerExecutorLayer =
    SweetMockitoLayer.newMockLayer[CrawlerExecutor]

  private val runAllPublishersExecutorLayer = ZLayer {
    ZIO
      .service[CrawlerExecutor]
      .map(RunAllPublishersExecutorImpl(_))
  }
