package toph.service

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import toph.TophSpec
import toph.centre.AccountService
import toph.context.TophExecutionContext
import toph.grpc.UserService
import toph.model.AccountFixture
import toph.security.Token
import toph.security.TokenFixture
import toph.service.ZioService.ZUserService
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object UserServiceSpec extends TophSpec:

  override def spec = suite("UserServiceSpec")(
    test("It should update a user.") {
      val updateUser             = updateUserFixture()
      val expectedAccount        = AccountFixture.createRandom()
      val expectedToken          = TokenFixture.createRandom().copy(account = expectedAccount)
      given TophExecutionContext = TophExecutionContext.identifiedAccount(expectedAccount)

      for
        _      <- SweetMockitoLayer[AccountService]
                    .whenF2(_.update(expectedAccount.key, AccountService.Update(updateUser.name)))
                    .thenReturn(expectedAccount)
        result <- ZIO.serviceWithZIO[ZUserService[Token]](_.update(updateUser, expectedToken))
      yield assertTrue(
        result == User(
          name = expectedAccount.name,
          email = expectedAccount.email,
        ),
      )
    },
  ).provideSome(
    SweetMockitoLayer.newMockLayer[AccountService],
    ZLayer {
      ZIO.serviceWithZIO[AccountService](centre => UserService(centre))
    },
  )

  private def updateUserFixture() = UpdateUser(
    name = KeyGenerator.generate(KeyLength.Medium),
  )
