package toph.repository

import core.KeyGenerator
import farango.DocumentCollection
import testkit.zio.testcontainers.ArangoDBLayer
import testkit.zio.testcontainers.FarangoLayer
import toph.fixture.EventFixture
import toph.repository.EventRepository
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.assertTrue
import zio.test.TestEnvironment
import toph.model.Event
import toph.IT
import EventRepository.given
import zio.test.TestAspect

object EventRepositoryIT extends IT:

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("An EventRepository")(
      suite("With Arango's container")(
        test("It should add a event into collection.") {
          val expectedEvent = EventFixture.createRandom()
          for
            repository <- ZIO.service[EventRepository]
            _          <- repository.add(expectedEvent)
            stored     <- FarangoLayer.getDocument[EventRepository.Document, Event](expectedEvent.key).some
          yield assertTrue(
            stored == expectedEvent
          )
        },
        test("It should remove a event from collection.") {
          val expectedEvent = EventFixture.createRandom()
          for
            repository <- ZIO.service[EventRepository]
            _          <- repository.add(expectedEvent)
            removed    <- repository.remove(expectedEvent.key).some
            notFound   <- FarangoLayer.getDocument[EventRepository.Document, Event](expectedEvent.key)
          yield assertTrue(
            removed == expectedEvent,
            notFound.isEmpty
          )
        }
      ).provideSome(
        ArangoDBLayer.layer,
        FarangoLayer.database,
        FarangoLayer.documentCollectionLayer(s"event_repository_${KeyGenerator.next4()}"),
        EventREpositoryLayer
      ) @@ TestAspect.sequential
    )

  private val EventREpositoryLayer = ZLayer {
    ZIO.serviceWith[DocumentCollection](EventRepository(_))
  }
