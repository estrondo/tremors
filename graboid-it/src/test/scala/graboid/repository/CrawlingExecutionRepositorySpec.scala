package graboid.repository

import graboid.CrawlingExecution
import graboid.CrawlingExecutionFixture
import graboid.time.ZonedDateTimeService
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import org.mockito.Mockito
import scala.util.Random
import tremors.zio.farango.CollectionManager
import tremors.zio.farango.FarangoTestContainer
import zio.Schedule
import zio.ZIO
import zio.ZLayer
import zio.durationInt
import zio.test.TestAspect
import zio.test.assertTrue

object CrawlingExecutionRepositorySpec extends GraboidItRepositorySpec:

  def spec = suite("CrawlingExecutionRepositorySpec")(
    test("It should insert a CrawlingExecution.") {
      val execution = CrawlingExecutionFixture.createNew()

      for inserted <- ZIO.serviceWithZIO[CrawlingExecutionRepository](_.insert(execution))
      yield assertTrue(inserted == execution)
    },
    test("It should update counting.") {
      val execution = CrawlingExecutionFixture.createNew()
      val updatedAt = execution.updatedAt.get.plusMinutes(3)
      val expected  = execution.copy(
        detected = Random.nextLong(100),
        updatedAt = Some(updatedAt)
      )
      for
        _       <- ZIO
                     .serviceWith[ZonedDateTimeService](service => Mockito.when(service.now()).thenReturn(updatedAt))
        _       <- ZIO.serviceWithZIO[CrawlingExecutionRepository](_.insert(execution))
        updated <- ZIO.serviceWithZIO[CrawlingExecutionRepository](
                     _.updateCounting(execution.copy(detected = expected.detected))
                   )
      yield assertTrue(updated == expected)
    },
    test("It should update the state.") {
      val execution = CrawlingExecutionFixture.createNew()
      val updatedAt = execution.updatedAt.get.plusMinutes(3)
      val expected  = execution.copy(
        updatedAt = Some(updatedAt),
        state = CrawlingExecution.State.Failed
      )
      for
        _       <- ZIO.serviceWith[ZonedDateTimeService](service => Mockito.when(service.now()).thenReturn(updatedAt))
        _       <- ZIO.serviceWithZIO[CrawlingExecutionRepository](_.insert(execution))
        updated <- ZIO.serviceWithZIO[CrawlingExecutionRepository](
                     _.updateState(execution.copy(state = expected.state))
                   )
      yield assertTrue(updated == expected)
    }
  ).provideSome(
    SweetMockitoLayer.newMockLayer[ZonedDateTimeService],
    FarangoTestContainer.arangoContainer,
    FarangoTestContainer.farangoDB,
    FarangoTestContainer.farangoDatabase(),
    FarangoTestContainer.farangoCollection(),
    ZLayer.fromFunction(CrawlingExecutionRepository.apply),
    ZLayer.fromFunction(CollectionManager.apply),
    ZLayer.succeed(Schedule.spaced(5.seconds))
  ) @@ TestAspect.sequential
