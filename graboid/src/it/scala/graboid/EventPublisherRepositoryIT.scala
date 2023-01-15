package graboid

import core.KeyGenerator
import farango.FarangoDatabase
import farango.FarangoDocumentCollection
import graboid.fixture.EventPublisherFixture
import graboid.layer.ArangoDBLayer
import graboid.layer.FarangoLayer
import zio.RLayer
import zio.Scope
import zio.URIO
import zio.ZIO
import zio.ZLayer
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.assertTrue
import ziorango.Ziorango
import ziorango.given

import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

object EventPublisherRepositoryIT extends IT:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("EventPublisherRepository with Arango's container")(
      test("it should insert a new publisher into collection.") {
        val publisher = EventPublisherFixture.createRandom()
        for
          repository <- getRepository
          result     <- repository.add(publisher)
          collection <- getCollection
          found      <- collection.get[EventPublisherRepository.Document, Ziorango.F](publisher.key).some
        yield assertTrue(
          result == publisher,
          found.name == publisher.name
        )
      },
      test("it should remove a publisher from collection.") {
        val publisher = EventPublisherFixture.createRandom()
        for
          repository <- getRepository
          _          <- repository.add(publisher)
          removed    <- repository.remove(publisher.key).some
          collection <- getCollection
          notFound   <- collection.get[EventPublisherRepository.Document, Ziorango.F](publisher.key)
        yield assertTrue(
          removed == publisher,
          notFound.isEmpty
        )
      },
      test("it should update a publisher in collection.") {
        val publisher      = EventPublisherFixture.createRandom()
        val expectedUpdate = EventPublisherFixture
          .updateFrom(EventPublisherFixture.createRandom())
        for
          repository <- getRepository
          _          <- repository.add(publisher)
          _          <- repository.update(publisher.key, expectedUpdate).some
          collection <- getCollection
          updated    <- collection.get[EventPublisherRepository.Document, Ziorango.F](publisher.key).some
        yield assertTrue(
          updated._key == publisher.key,
          updated.name == expectedUpdate.name,
          updated.beginning == expectedUpdate.beginning.getLong(ChronoField.INSTANT_SECONDS),
          updated.ending.longValue() == expectedUpdate.ending.get.getLong(ChronoField.INSTANT_SECONDS)
        )
      }
    ).provideSomeLayer(ArangoDBLayer.layer >>> FarangoLayer.database >>> RepositoryLayer) @@ TestAspect.sequential

  private val getRepository: URIO[TestLayer, EventPublisherRepository] =
    ZIO.serviceWith[TestLayer](_._1)

  private val getCollection: URIO[TestLayer, FarangoDocumentCollection] =
    ZIO.serviceWith[TestLayer](_._2)

  private type TestLayer = (EventPublisherRepository, FarangoDocumentCollection)

  private val RepositoryLayer: RLayer[FarangoDatabase, TestLayer] = ZLayer {
    for collection <- FarangoLayer.documentCollection(s"event_publisher_${KeyGenerator.next4()}")
    yield (EventPublisherRepository(collection), collection)
  }
