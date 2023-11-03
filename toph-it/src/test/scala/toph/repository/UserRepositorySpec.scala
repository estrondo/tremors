package toph.repository

import one.estrondo.farango.Collection
import one.estrondo.farango.Database
import one.estrondo.farango.sync.SyncCollection
import toph.model.TophUserFixture
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.zio.farango.CollectionManager
import tremors.zio.farango.FarangoTestContainer
import zio.ZIO
import zio.ZLayer
import zio.test.TestAspect
import zio.test.assertTrue

object UserRepositorySpec extends TophRepositorySpec:

  override def spec = suite("UserRepositorySpec")(
    test("It should add an user in database.") {
      val expectedUser = TophUserFixture.createRandom()
      for
        _      <- ZIO.serviceWithZIO[UserRepository](_.add(expectedUser))
        stored <- ZIO.serviceWith[SyncCollection] {
                    _.arango.getDocument(expectedUser.id, classOf[UserRepository.Stored])
                  }
      yield assertTrue(
        stored._key == expectedUser.id,
        stored.name == expectedUser.name,
        stored.email == expectedUser.email
      )
    },
    test("It should update a stored user in database.") {
      val addedUser = TophUserFixture.createRandom()
      val newName   = s"Albert ${KeyGenerator.generate(KeyLength.Long)}."
      for
        _       <- ZIO.serviceWithZIO[UserRepository](_.add(addedUser))
        _       <- ZIO.serviceWithZIO[UserRepository](_.update(addedUser.id, UserRepository.Update(newName)))
        updated <- ZIO.serviceWith[SyncCollection] {
                     _.arango.getDocument(addedUser.id, classOf[UserRepository.Stored])
                   }
      yield assertTrue(
        updated.name == newName,
        updated._key == addedUser.id,
        updated.email == addedUser.email
      )
    },
    test("It should find an user by email") {
      val expectedUser = TophUserFixture.createRandom()
      for
        _      <- ZIO.serviceWithZIO[UserRepository](_.add(expectedUser))
        result <- ZIO.serviceWithZIO[UserRepository](_.searchByEmail(expectedUser.email))
      yield assertTrue(
        result.nonEmpty
      )
    }
  ).provideSome(
    FarangoTestContainer.arangoContainer,
    FarangoTestContainer.farangoDatabase(),
    FarangoTestContainer.farangoDB,
    FarangoTestContainer.farangoCollection(),
    collectionManagerLayer,
    ZLayer {
      ZIO.serviceWithZIO[CollectionManager] { manager =>
        UserRepository(manager)
      }
    }
  ) @@ TestAspect.sequential
