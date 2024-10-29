package graboid

import graboid.command.CommandExecutor
import graboid.command.CrawlingCommandExecutor
import graboid.command.DataCentreCommandExecutor
import graboid.protocol.CrawlingCommand
import graboid.protocol.DataCentreCommand
import graboid.protocol.GraboidCommandFixture
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.TestResult
import zio.test.assertTrue

object CommandExecutorSpec extends GraboidSpec:

  // noinspection TypeAnnotation
  override def spec = suite("A CommandExecutor:")(
    suite("When it receives any DataCentre's command:")(
      dataCentreTest(GraboidCommandFixture.createDataCentre()),
      dataCentreTest(GraboidCommandFixture.updateDataCentre()),
      dataCentreTest(GraboidCommandFixture.updateDataCentre()),
      crawlingTest(GraboidCommandFixture.runEventCrawling()),
      crawlingTest(GraboidCommandFixture.runDataCentreEventCrawling()),
    ),
  ).provideSome[Scope](
    SweetMockitoLayer.newMockLayer[DataCentreCommandExecutor],
    SweetMockitoLayer.newMockLayer[CrawlingCommandExecutor],
    ZLayer {
      for
        dataCentreCommandExecutor <- ZIO.service[DataCentreCommandExecutor]
        crawlingCommandExecutor   <- ZIO.service[CrawlingCommandExecutor]
        executor                  <- CommandExecutor(dataCentreCommandExecutor, crawlingCommandExecutor)
      yield executor
    },
  )

  def dataCentreTest(command: DataCentreCommand): Spec[CommandExecutor & DataCentreCommandExecutor, Throwable] =
    test(s"It should redirect a ${command.getClass.getSimpleName} to DataCentreExecutor.") {
      for
        _      <- SweetMockitoLayer[DataCentreCommandExecutor]
                    .whenF2(_(command))
                    .thenReturn(command)
        result <- ZIO.serviceWithZIO[CommandExecutor](_(command))
      yield assertTrue(
        result == command,
      )
    }

  def crawlingTest(command: CrawlingCommand): Spec[CommandExecutor & CrawlingCommandExecutor, Throwable] =
    test(s"It should redirect a ${command.getClass.getSimpleName} to CrawlingCommandExecutor.") {
      for
        _      <- SweetMockitoLayer[CrawlingCommandExecutor]
                    .whenF2(_(command))
                    .thenReturn(command)
        result <- ZIO.serviceWithZIO[CommandExecutor](_(command))
      yield assertTrue(
        result == command,
      )
    }
