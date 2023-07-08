package graboid.repository
import graboid.manager.FDSNDataCentreFixture
import one.estrondo.farango.Collection
import one.estrondo.farango.sync.SyncCollection
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.zio.farango.FarangoTestContainer
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.assertTrue

object FDSNDataCentreRepositoryItSpec extends GraboidItRepositorySpec:

  override def spec = suite("FDSNDataCentreRepositoryItSpec")(
    test("It should insert a data centre into the collection.") {
      val expected = FDSNDataCentreFixture.createRandom()

      for
        _      <- ZIO.serviceWithZIO[FDSNDataCentreRepository](_.insert(expected))
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
      val expected    = FDSNDataCentreFixture.createRandom()
      val expectedUrl = KeyGenerator.generate(KeyLength.Long)

      for
        _      <- ZIO.serviceWithZIO[FDSNDataCentreRepository](_.insert(expected))
        _      <- ZIO.serviceWithZIO[FDSNDataCentreRepository](_.update(expected.copy(url = expectedUrl)))
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
      val expected = FDSNDataCentreFixture.createRandom()

      for
        _      <- ZIO.serviceWithZIO[FDSNDataCentreRepository](_.insert(expected))
        _      <- ZIO.serviceWithZIO[FDSNDataCentreRepository](_.delete(expected.id))
        stored <- ZIO.serviceWithZIO[SyncCollection](x =>
                    ZIO.attemptBlocking {
                      x.arango.getDocument(expected.id, classOf[Stored])
                    }
                  )
      yield assertTrue(stored == null)
    },
    test("It should get a data centre from the collection.") {
      val expected = FDSNDataCentreFixture.createRandom()

      for
        _   <- ZIO.serviceWithZIO[FDSNDataCentreRepository](_.insert(expected))
        got <- ZIO.serviceWithZIO[FDSNDataCentreRepository](_.get(expected.id))
      yield assertTrue(
        got.contains(expected)
      )
    },
    test("It should list all data centres from the collection.") {
      val expectedOnes = (for _ <- 0 until 10 yield FDSNDataCentreFixture.createRandom()).toSet
      for
        _         <- ZIO.foreach(expectedOnes)(expected => ZIO.serviceWithZIO[FDSNDataCentreRepository](_.insert(expected)))
        collected <- ZIO.serviceWithZIO[FDSNDataCentreRepository](_.all.runCollect).map(_.toSet)
      yield assertTrue(
        collected == expectedOnes
      )
    }
  ).provideSome(
    ZLayer.fromFunction(FDSNDataCentreRepository.apply),
    FarangoTestContainer.arangoContainer,
    FarangoTestContainer.farangoDB,
    FarangoTestContainer.farangoDatabase,
    FarangoTestContainer.farangoCollection()
  )

  case class Stored(_key: String, url: String)
