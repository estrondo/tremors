package toph.repository

import one.estrondo.farango.Collection
import one.estrondo.farango.Database
import one.estrondo.farango.sync.SyncCollection
import toph.model.AccountFixture
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.zio.farango.CollectionManager
import tremors.zio.farango.FarangoTestContainer
import zio.ZIO
import zio.ZLayer
import zio.test.TestAspect
import zio.test.assertTrue

object AccountRepositorySpec extends TophRepositorySpec:

  override def spec = suite("AccountRepositorySpec")(
    test("It should add an account to database.") {
      val expectedAccount = AccountFixture.createRandom()
      for
        _      <- ZIO.serviceWithZIO[AccountRepository](_.add(expectedAccount))
        stored <- ZIO.serviceWith[SyncCollection] {
                    _.arango.getDocument(expectedAccount.key, classOf[AccountRepository.Stored])
                  }
      yield assertTrue(
        stored._key == expectedAccount.key,
        stored.name == expectedAccount.name,
        stored.email == expectedAccount.email,
      )
    },
    test("It should update a stored account in database.") {
      val expectedAccount = AccountFixture.createRandom()
      val newName   = s"Albert ${KeyGenerator.generate(KeyLength.Long)}."
      for
        _       <- ZIO.serviceWithZIO[AccountRepository](_.add(expectedAccount))
        _       <- ZIO.serviceWithZIO[AccountRepository](_.update(expectedAccount.key, AccountRepository.Update(newName)))
        updated <- ZIO.serviceWith[SyncCollection] {
                     _.arango.getDocument(expectedAccount.key, classOf[AccountRepository.Stored])
                   }
      yield assertTrue(
        updated.name == newName,
        updated._key == expectedAccount.key,
        updated.email == expectedAccount.email,
      )
    },
    test("It should find an account by email") {
      val expectedAccount = AccountFixture.createRandom()
      for
        _      <- ZIO.serviceWithZIO[AccountRepository](_.add(expectedAccount))
        result <- ZIO.serviceWithZIO[AccountRepository](_.searchByEmail(expectedAccount.email))
      yield assertTrue(
        result.nonEmpty,
      )
    },
  ).provideSome(
    FarangoTestContainer.arangoContainer,
    FarangoTestContainer.farangoDatabase(),
    FarangoTestContainer.farangoDB,
    FarangoTestContainer.farangoCollection(),
    collectionManagerLayer,
    ZLayer {
      ZIO.serviceWithZIO[CollectionManager] { manager =>
        AccountRepository(manager)
      }
    },
  ) @@ TestAspect.sequential
