package graboid.command

import graboid.FDSNDataCentre
import graboid.GraboidSpec
import graboid.manager.FDSNDataCentreManager
import graboid.protocol.DataCentreCommand
import graboid.protocol.GraboidCommandFixture
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.assertTrue

object DataCentreExecutorSpec extends GraboidSpec:

  // noinspection TypeAnnotation
  def spec = suite("A DataCentreExecutor")(
    testWellDoneCommand(GraboidCommandFixture.createDataCentre()) { (manager, c) =>
      val dataCentre = FDSNDataCentre(c.id, c.url)
      SweetMockito.whenF2(manager.add(dataCentre)).thenReturn(dataCentre)
    },
    testWellDoneCommand(GraboidCommandFixture.updateDataCentre()) { (manager, c) =>
      val dataCentre = FDSNDataCentre(c.id, c.url)
      SweetMockito.whenF2(manager.update(dataCentre)).thenReturn(dataCentre)
    },
    testWellDoneCommand(GraboidCommandFixture.deleteDataCentre()) { (manager, c) =>
      val dataCentre = FDSNDataCentre(c.id, KeyGenerator.generate(KeyLength.Long))
      SweetMockito.whenF2(manager.delete(c.id)).thenReturn(dataCentre)
    }
  ).provideSome[Scope](
    SweetMockitoLayer.newMockLayer[FDSNDataCentreManager],
    ZLayer {
      ZIO.serviceWithZIO[FDSNDataCentreManager](DataCentreExecutor.apply)
    }
  )

  def testWellDoneCommand[C <: DataCentreCommand](command: C)(
      fn: (FDSNDataCentreManager, C) => Unit
  ): Spec[DataCentreExecutor & FDSNDataCentreManager, Throwable] =
    test(s"It should execute a ${command.getClass.getSimpleName} command.") {
      for
        _      <- ZIO.serviceWith[FDSNDataCentreManager](manager => fn(manager, command))
        result <- ZIO.serviceWithZIO[DataCentreExecutor](_(command))
      yield assertTrue(result == command)
    }
