package graboid

import graboid.command.DataCentreExecutor
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
      dataCentreTest(GraboidCommandFixture.updateDataCentre())
    )
  ).provideSome[Scope](
    SweetMockitoLayer.newMockLayer[DataCentreExecutor],
    ZLayer {
      for
        dataCentreExecutor <- ZIO.service[DataCentreExecutor]
        executor           <- CommandExecutor(dataCentreExecutor)
      yield executor
    }
  )

  def dataCentreTest(command: DataCentreCommand): Spec[CommandExecutor & DataCentreExecutor, Throwable] =
    test(s"It should redirect a ${command.getClass.getSimpleName} to DataCentreExecutor.") {
      for
        _      <- SweetMockitoLayer[DataCentreExecutor]
                    .whenF2(_(command))
                    .thenReturn(command)
        result <- ZIO.serviceWithZIO[CommandExecutor](_(command))
      yield assertTrue(
        result == command
      )
    }
