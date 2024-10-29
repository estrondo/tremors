package toph.repository

import toph.model.TophEventFixture
import tremors.zio.farango.CollectionManager
import tremors.zio.farango.FarangoTestContainer
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object EventRepositorySpec extends TophRepositorySpec:

  def spec = suite("An EventRepository")(
    test("It should add an Event in the database.") {
      val expectedTophEvent = TophEventFixture.createRandom()
      for stored <- ZIO.serviceWithZIO[EventRepository](_.add(expectedTophEvent))
      yield assertTrue(
        stored == expectedTophEvent,
      )
    },
  ).provideSome(
    FarangoTestContainer.arangoContainer,
    FarangoTestContainer.farangoDB,
    FarangoTestContainer.farangoDatabase(),
    FarangoTestContainer.farangoCollection(),
    collectionManagerLayer,
    ZLayer {
      ZIO.serviceWithZIO[CollectionManager](EventRepository.apply)
    },
  )
