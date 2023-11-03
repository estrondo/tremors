package toph.centre

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import toph.TophSpec
import toph.model.TophEventFixture
import toph.repository.EventRepository
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object EventCentreSpec extends TophSpec:

  def spec = suite("An EventCentre")(
    test("It should add a new event.") {
      val expectedTophEvent = TophEventFixture.createRandom()

      for
        _          <- SweetMockitoLayer[EventRepository].whenF2(_.add(expectedTophEvent)).thenReturn(expectedTophEvent)
        addedEvent <-
          ZIO.serviceWithZIO[EventCentre](_.add(expectedTophEvent))
      yield assertTrue(
        addedEvent == expectedTophEvent
      )
    }
  ).provideSome(
    SweetMockitoLayer.newMockLayer[EventRepository],
    ZLayer {
      for repository <- ZIO.service[EventRepository]
      yield EventCentre(repository)
    }
  )
