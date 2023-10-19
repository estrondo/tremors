package toph.service

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import toph.TophSpec
import toph.centre.UserCentre
import toph.model.AuthenticatedUser
import toph.model.AuthenticatedUserFixture
import toph.model.TophUserFixture
import toph.service.ZioService.ZUserService
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object UserServiceSpec extends TophSpec:

  override def spec = suite("UserServiceSpec")(
    test("It should update a user.") {
      val updateUser        = updateUserFixture()
      val expectedUser      = TophUserFixture.createRandom()
      val authenticatedUser = AuthenticatedUserFixture.createRandom()

      for
        _      <- SweetMockitoLayer[UserCentre]
                    .whenF2(_.update(authenticatedUser.claims.id, UserCentre.Update(updateUser.name)))
                    .thenReturn(expectedUser)
        result <- ZIO.serviceWithZIO[ZUserService[AuthenticatedUser]](_.update(updateUser, authenticatedUser))
      yield assertTrue(
        result == User(
          name = expectedUser.name,
          email = expectedUser.email
        )
      )
    }
  ).provideSome(
    SweetMockitoLayer.newMockLayer[UserCentre],
    ZLayer {
      ZIO.serviceWithZIO[UserCentre](centre => UserService(centre))
    }
  )

  private def updateUserFixture() = UpdateUser(
    name = KeyGenerator.generate(KeyLength.Medium)
  )
