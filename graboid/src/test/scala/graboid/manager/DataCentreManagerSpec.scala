package graboid.manager

import graboid.GraboidSpec
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.assertTrue

object DataCentreManagerSpec extends GraboidSpec:

  override def spec = suite("'DataCentreManagerSpec'")(
    test("When a data centre is added it should call the repository.") {
      val expectedDataCentre = DataCentreFixture.createRandom()

      for
        _          <- SweetMockitoLayer[DataCentreManager]
                        .whenF2(_.add(expectedDataCentre))
                        .thenReturn(expectedDataCentre)
        dataCentre <- ZIO.serviceWithZIO[DataCentreManager](_.add(expectedDataCentre))
      yield assertTrue(dataCentre == expectedDataCentre)
    },
    test("When a data centre is updated it should call the repository.") {
      val expectedDataCentre = DataCentreFixture.createRandom()

      for
        _          <- SweetMockitoLayer[DataCentreManager]
                        .whenF2(_.update(expectedDataCentre))
                        .thenReturn(expectedDataCentre)
        dataCentre <- ZIO.serviceWithZIO[DataCentreManager](_.update(expectedDataCentre))
      yield assertTrue(dataCentre == expectedDataCentre)
    },
    test("When a data centre is deleted it should call the repository.") {
      val expectedDataCentre = DataCentreFixture.createRandom()

      for
        _          <- SweetMockitoLayer[DataCentreManager]
                        .whenF2(_.delete(expectedDataCentre.id))
                        .thenReturn(expectedDataCentre)
        dataCentre <- ZIO.serviceWithZIO[DataCentreManager](_.delete(expectedDataCentre.id))
      yield assertTrue(dataCentre == expectedDataCentre)
    },
    test("When a data centre is read it should call the repository.") {
      val expectedDataCentre = DataCentreFixture.createRandom()

      for
        _          <- SweetMockitoLayer[DataCentreManager]
                        .whenF2(_.get(expectedDataCentre.id))
                        .thenReturn(Some(expectedDataCentre))
        dataCentre <- ZIO.serviceWithZIO[DataCentreManager](_.get(expectedDataCentre.id))
      yield assertTrue(dataCentre == Some(expectedDataCentre))
    },
    test("It should return a stream of all data centres.") {
      val expected = DataCentreFixture.createRandom()

      for
        _         <- SweetMockitoLayer[DataCentreManager]
                       .whenF2(_.all)
                       .thenReturn(expected)
        stream    <- ZIO.serviceWith[DataCentreManager](_.all)
        collected <- stream.runCollect
      yield assertTrue(collected == List(expected))
    }
  ).provideSome[Scope](
    SweetMockitoLayer.newMockLayer[DataCentreManager]
  )
