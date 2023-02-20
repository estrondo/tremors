package webapi.manager

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import webapi.Spec
import webapi.fixture.UserFixture
import webapi.repository.UserRepository
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue
import webapi.model.User

object UserManagerSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A UserManager")(
      test("It should add an user.") {
        val expectedUser = UserFixture.createRandom()
        for
          _      <- SweetMockitoLayer[UserRepository]
                      .whenF2(_.add(expectedUser))
                      .thenReturn(expectedUser)
          result <- ZIO.serviceWithZIO[UserManager](_.add(expectedUser))
        yield assertTrue(
          result == expectedUser
        )
      },
      test("It should get an user by email.") {
        val expectedUser = UserFixture.createRandom()
        for
          _      <- SweetMockitoLayer[UserRepository]
                      .whenF2(_.get(expectedUser.email))
                      .thenReturn(Some(expectedUser))
          result <- ZIO.serviceWithZIO[UserManager](_.get(expectedUser.email))
        yield assertTrue(
          result == Some(expectedUser)
        )
      },
      test("It should update an user.") {
        val user   = UserFixture.createRandom()
        val update = User.Update(name = "Tesla")

        for
          _      <- SweetMockitoLayer[UserRepository]
                      .whenF2(_.update(user.email, update))
                      .thenReturn(Some(user))
          result <- ZIO.serviceWithZIO[UserManager](_.update(user.email, update))
        yield assertTrue(
          result == Some(user)
        )
      },
      test("It should remove an user.") {
        val user = UserFixture.createRandom()

        for
          _      <- SweetMockitoLayer[UserRepository]
                      .whenF2(_.remove(user.email))
                      .thenReturn(Some(user))
          result <- ZIO.serviceWithZIO[UserManager](_.remove(user.email))
        yield assertTrue(
          result == Some(user)
        )
      }
    ).provideSome(
      SweetMockitoLayer.newMockLayer[UserRepository],
      ZLayer(ZIO.serviceWith[UserRepository](UserManager.apply))
    )
