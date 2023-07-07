package graboid.manager

import graboid.GraboidSpec
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.assertTrue

object FDSNDataCentreManagerSpec extends GraboidSpec:

  override def spec = suite("FDSNDataCentreManagerSpec")(
    test("When a data centre is added it should call the repository.") {
      val expectedDataCentre = FDSNDataCentreFixture.createRandom()

      for
        _          <- SweetMockitoLayer[FDSNDataCentreManager]
                        .whenF2(_.add(expectedDataCentre))
                        .thenReturn(expectedDataCentre)
        dataCentre <- ZIO.serviceWithZIO[FDSNDataCentreManager](_.add(expectedDataCentre))
      yield assertTrue(dataCentre == expectedDataCentre)
    },
    test("When a data centre is updated it should call the repository.") {
      val expectedDataCentre = FDSNDataCentreFixture.createRandom()

      for
        _          <- SweetMockitoLayer[FDSNDataCentreManager]
                        .whenF2(_.update(expectedDataCentre))
                        .thenReturn(expectedDataCentre)
        dataCentre <- ZIO.serviceWithZIO[FDSNDataCentreManager](_.update(expectedDataCentre))
      yield assertTrue(dataCentre == expectedDataCentre)
    },
    test("When a data centre is deleted it should call the repository.") {
      val expectedDataCentre = FDSNDataCentreFixture.createRandom()

      for
        _          <- SweetMockitoLayer[FDSNDataCentreManager]
                        .whenF2(_.delete(expectedDataCentre.id))
                        .thenReturn(expectedDataCentre)
        dataCentre <- ZIO.serviceWithZIO[FDSNDataCentreManager](_.delete(expectedDataCentre.id))
      yield assertTrue(dataCentre == expectedDataCentre)
    },
    test("When a data centre is read it should call the repository.") {
      val expectedDataCentre = FDSNDataCentreFixture.createRandom()

      for
        _          <- SweetMockitoLayer[FDSNDataCentreManager]
                        .whenF2(_.get(expectedDataCentre.id))
                        .thenReturn(Some(expectedDataCentre))
        dataCentre <- ZIO.serviceWithZIO[FDSNDataCentreManager](_.get(expectedDataCentre.id))
      yield assertTrue(dataCentre == Some(expectedDataCentre))
    },
    test("It should return a stream of all data centres.") {
      val expected = FDSNDataCentreFixture.createRandom()

      for
        _         <- SweetMockitoLayer[FDSNDataCentreManager]
                       .whenF2(_.all)
                       .thenReturn(expected)
        stream    <- ZIO.serviceWith[FDSNDataCentreManager](_.all)
        collected <- stream.runCollect
      yield assertTrue(collected == List(expected))
    }
  ).provideSome[Scope](
    SweetMockitoLayer.newMockLayer[FDSNDataCentreManager]
  )
