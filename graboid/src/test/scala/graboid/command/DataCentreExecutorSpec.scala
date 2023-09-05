package graboid.command

import graboid.DataCentre
import graboid.GraboidSpec
import graboid.manager.DataCentreManager
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
      val dataCentre = DataCentre(c.id, c.event, c.dataselect)
      SweetMockito.whenF2(manager.add(dataCentre)).thenReturn(dataCentre)
    },
    testWellDoneCommand(GraboidCommandFixture.updateDataCentre()) { (manager, c) =>
      val dataCentre = DataCentre(c.id, c.event, c.dataselect)
      SweetMockito.whenF2(manager.update(dataCentre)).thenReturn(dataCentre)
    },
    testWellDoneCommand(GraboidCommandFixture.deleteDataCentre()) { (manager, c) =>
      val dataCentre =
        DataCentre(c.id, Some(KeyGenerator.generate(KeyLength.Long)), Some(KeyGenerator.generate(KeyLength.Short)))
      SweetMockito.whenF2(manager.delete(c.id)).thenReturn(dataCentre)
    }
  ).provideSome[Scope](
    SweetMockitoLayer.newMockLayer[DataCentreManager],
    ZLayer {
      ZIO.serviceWithZIO[DataCentreManager](DataCentreExecutor.apply)
    }
  )

  def testWellDoneCommand[C <: DataCentreCommand](command: C)(
      fn: (DataCentreManager, C) => Unit
  ): Spec[DataCentreExecutor & DataCentreManager, Throwable] =
    test(s"It should execute a ${command.getClass.getSimpleName} command.") {
      for
        _      <- ZIO.serviceWith[DataCentreManager](manager => fn(manager, command))
        result <- ZIO.serviceWithZIO[DataCentreExecutor](_(command))
      yield assertTrue(result == command)
    }
