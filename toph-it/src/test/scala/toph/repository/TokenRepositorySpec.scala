package toph.repository

import toph.model.TokenFixture
import tremors.zio.farango.CollectionManager
import tremors.zio.farango.FarangoTestContainer
import zio.ZIO
import zio.ZLayer
import zio.test.*

object TokenRepositorySpec extends TophRepositorySpec:

  def spec = suite("TokenRepository")(
    test("It should store a token.") {
      val expectedToken = TokenFixture.createRandom()
      for exit <- ZIO.serviceWithZIO[TokenRepository](_.add(expectedToken)).exit
      yield assertTrue(
        exit.isSuccess,
      )
    },
  ).provideSome(
    FarangoTestContainer.arangoContainer,
    FarangoTestContainer.farangoDB,
    FarangoTestContainer.farangoDatabase(),
    FarangoTestContainer.farangoCollection(),
    collectionManagerLayer,
    ZLayer.fromFunction(TokenRepository.apply),
  )
