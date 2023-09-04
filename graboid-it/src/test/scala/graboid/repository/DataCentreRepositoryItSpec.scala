package graboid.repository
import graboid.manager.DataCentreFixture
import one.estrondo.farango.Collection
import one.estrondo.farango.Database
import one.estrondo.farango.sync.SyncCollection
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.zio.farango.CollectionManager
import tremors.zio.farango.FarangoTestContainer
import zio.Schedule
import zio.ZIO
import zio.ZLayer
import zio.durationInt
import zio.test.Spec
import zio.test.TestAspect
import zio.test.assertTrue

object DataCentreRepositoryItSpec extends GraboidItRepositorySpec:

  override def spec = suite("DataCentreRepositoryItSpec")(
    test("It should insert a data centre into the collection.") {
      val expected = DataCentreFixture.createRandom()

      for
        _      <- ZIO.serviceWithZIO[DataCentreRepository](_.insert(expected))
        stored <- ZIO.serviceWithZIO[SyncCollection](x =>
                    ZIO.attemptBlocking {
                      x.arango.getDocument(expected.id, classOf[Stored])
                    }
                  )
      yield assertTrue(
        stored.url == expected.url,
        stored._key == expected.id
      )
    },
    test("It should update a data centre into the collection.") {
      val expected    = DataCentreFixture.createRandom()
      val expectedUrl = KeyGenerator.generate(KeyLength.Long)

      for
        _      <- ZIO.serviceWithZIO[DataCentreRepository](_.insert(expected))
        _      <- ZIO.serviceWithZIO[DataCentreRepository](_.update(expected.copy(url = expectedUrl)))
        stored <- ZIO.serviceWithZIO[SyncCollection](x =>
                    ZIO.attemptBlocking {
                      x.arango.getDocument(expected.id, classOf[Stored])
                    }
                  )
      yield assertTrue(
        stored.url == expectedUrl,
        stored._key == expected.id
      )
    },
    test("It should delete a data centre from the collection.") {
      val expected = DataCentreFixture.createRandom()

      for
        _      <- ZIO.serviceWithZIO[DataCentreRepository](_.insert(expected))
        _      <- ZIO.serviceWithZIO[DataCentreRepository](_.delete(expected.id))
        stored <- ZIO.serviceWithZIO[SyncCollection](x =>
                    ZIO.attemptBlocking {
                      x.arango.getDocument(expected.id, classOf[Stored])
                    }
                  )
      yield assertTrue(stored == null)
    },
    test("It should get a data centre from the collection.") {
      val expected = DataCentreFixture.createRandom()

      for
        _   <- ZIO.serviceWithZIO[DataCentreRepository](_.insert(expected))
        got <- ZIO.serviceWithZIO[DataCentreRepository](_.get(expected.id))
      yield assertTrue(
        got.contains(expected)
      )
    },
    test("It should list all data centres from the collection.") {
      val expectedOnes = (for _ <- 0 until 10 yield DataCentreFixture.createRandom()).toSet
      for
        _         <- ZIO.foreach(expectedOnes)(expected => ZIO.serviceWithZIO[DataCentreRepository](_.insert(expected)))
        collected <- ZIO.serviceWithZIO[DataCentreRepository](_.all.runCollect).map(_.toSet)
      yield assertTrue(
        collected == expectedOnes
      )
    }
  ).provideSome(
    ZLayer.fromFunction(DataCentreRepository.apply),
    FarangoTestContainer.arangoContainer,
    FarangoTestContainer.farangoDB,
    FarangoTestContainer.farangoDatabase(),
    FarangoTestContainer.farangoCollection(),
    ZLayer {
      for
        database   <- ZIO.service[Database]
        collection <- ZIO.service[Collection]
      yield CollectionManager(collection, database, Schedule.spaced(1.second))
    }
  ) @@ TestAspect.sequential

  case class Stored(_key: String, url: String)
