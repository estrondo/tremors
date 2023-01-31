package graboid

import core.KeyGenerator
import farango.Database
import farango.DocumentCollection
import farango.zio.ZEffect
import graboid.fixture.PublisherFixture
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

import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

object PublisherRepositoryIT extends IT:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("PublisherRepository with Arango's container")(
      test("it should insert a new publisher into collection.") {
        val publisher = PublisherFixture.createRandom()
        for
          repository <- getRepository
          result     <- repository.add(publisher)
          collection <- getCollection
          found      <- collection.getT[PublisherRepository.Document, ZEffect](publisher.key).some
        yield assertTrue(
          result == publisher,
          found.name == publisher.name
        )
      },
      test("it should remove a publisher from collection.") {
        val publisher = PublisherFixture.createRandom()
        for
          repository <- getRepository
          _          <- repository.add(publisher)
          removed    <- repository.remove(publisher.key).some
          collection <- getCollection
          notFound   <- collection.getT[PublisherRepository.Document, ZEffect](publisher.key)
        yield assertTrue(
          removed == publisher,
          notFound.isEmpty
        )
      },
      test("it should update a publisher in collection.") {
        val publisher      = PublisherFixture.createRandom()
        val expectedUpdate = PublisherFixture
          .updateFrom(PublisherFixture.createRandom())
        for
          repository <- getRepository
          _          <- repository.add(publisher)
          _          <- repository.update(publisher.key, expectedUpdate).some
          collection <- getCollection
          updated    <- collection.getT[PublisherRepository.Document, ZEffect](publisher.key).some
        yield assertTrue(
          updated._key == publisher.key,
          updated.name == expectedUpdate.name,
          updated.beginning == expectedUpdate.beginning.getLong(ChronoField.INSTANT_SECONDS),
          updated.ending.longValue() == expectedUpdate.ending.get.getLong(ChronoField.INSTANT_SECONDS)
        )
      }
    ).provideSomeLayer(ArangoDBLayer.layer >>> FarangoLayer.database >>> RepositoryLayer) @@ TestAspect.sequential

  private val getRepository: URIO[TestLayer, PublisherRepository] =
    ZIO.serviceWith[TestLayer](_._1)

  private val getCollection: URIO[TestLayer, DocumentCollection] =
    ZIO.serviceWith[TestLayer](_._2)

  private type TestLayer = (PublisherRepository, DocumentCollection)

  private val RepositoryLayer: RLayer[Database, TestLayer] = ZLayer {
    for collection <- FarangoLayer.documentCollection(s"publisher_${KeyGenerator.next4()}")
    yield (PublisherRepository(collection), collection)
  }