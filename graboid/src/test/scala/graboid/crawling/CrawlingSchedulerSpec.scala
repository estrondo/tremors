package graboid.crawling

import graboid.CrawlingSchedulingFixture
import graboid.GraboidSpec
import graboid.manager.DataCentreFixture
import graboid.repository.CrawlingSchedulingRepository
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.Mockito
import tremors.ZonedDateTimeFixture
import zio.ZIO
import zio.ZLayer
import zio.stream.ZStream
import zio.test.assertTrue

object CrawlingSchedulerSpec extends GraboidSpec:

  def spec = suite("CrawlingSchedulerSpec")(
    test("It should add a scheduling.") {
      val dataCentre = DataCentreFixture.createRandom()
      val expected   = CrawlingSchedulingFixture.createRandom(dataCentre)
      for
        _     <- SweetMockitoLayer[CrawlingSchedulingRepository].whenF2(_.insert(expected)).thenReturn(expected)
        added <- ZIO.serviceWithZIO[CrawlingScheduler](_.add(expected))
      yield assertTrue(added == expected)
    },
    test("It should update a scheduling.") {
      val dataCentre = DataCentreFixture.createRandom()
      val expected   = CrawlingSchedulingFixture.createRandom(dataCentre)
      for
        _     <- SweetMockitoLayer[CrawlingSchedulingRepository].whenF2(_.update(expected)).thenReturn(expected)
        added <- ZIO.serviceWithZIO[CrawlingScheduler](_.update(expected))
      yield assertTrue(added == expected)
    },
    test("It should remove a scheduling") {
      val dataCentre = DataCentreFixture.createRandom()
      val expected   = CrawlingSchedulingFixture.createRandom(dataCentre)
      for
        _     <- SweetMockitoLayer[CrawlingSchedulingRepository].whenF2(_.delete(expected.id)).thenReturn(expected)
        added <- ZIO.serviceWithZIO[CrawlingScheduler](_.remove(expected.id))
      yield assertTrue(added == expected)
    },
    test("It should search for scheduling at a specific moment.") {
      val dataCentre   = DataCentreFixture.createRandom()
      val expectedOnes = for (_ <- 0 until 10) yield CrawlingSchedulingFixture.createRandom(dataCentre)
      val moment       = ZonedDateTimeFixture.createRandom()
      for
        _      <- ZIO.serviceWith[CrawlingSchedulingRepository](mock =>
                    Mockito.when(mock.search(moment)).thenReturn(ZStream.fromIterable(expectedOnes))
                  )
        result <- ZIO.serviceWithZIO[CrawlingScheduler](_.search(moment).runCollect)
      yield assertTrue(expectedOnes == result)
    }
  ).provideSome(
    SweetMockitoLayer.newMockLayer[CrawlingSchedulingRepository],
    ZLayer {
      ZIO.serviceWithZIO(CrawlingScheduler.apply)
    }
  )
