package toph.grpc.impl

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import toph.TophSpec
import toph.context.TophExecutionContext
import toph.model.AccountFixture
import toph.security.Token
import toph.security.TokenFixture
import toph.service.AccountService
import toph.v1.grpc.GrpcAccount
import toph.v1.grpc.GrpcUpdateAccount
import toph.v1.grpc.ZioGrpc
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object GRPCAccountServiceSpec extends TophSpec:

  override def spec = suite("The GRPCAccountService")(
    test("it should update a user.") {
      val updateUser             = updateUserFixture()
      val expectedAccount        = AccountFixture.createRandom()
      val expectedToken          = TokenFixture.createRandom().copy(account = expectedAccount)
      given TophExecutionContext = TophExecutionContext.account(expectedAccount)

      for
        _      <- SweetMockitoLayer[AccountService]
                    .whenF2(_.update(expectedAccount.key, AccountService.Update(updateUser.name)))
                    .thenReturn(expectedAccount)
        result <- ZIO.serviceWithZIO[ZioGrpc.ZAccountService[Token]](_.update(updateUser, expectedToken))
      yield assertTrue(
        result == GrpcAccount(
          key = expectedAccount.key,
          name = expectedAccount.name,
          email = expectedAccount.email,
        ),
      )
    },
  ).provideSome(
    SweetMockitoLayer.newMockLayer[AccountService],
    ZLayer {
      ZIO.serviceWithZIO[AccountService](accountService => GRPCAccountService(accountService))
    },
  )

  private def updateUserFixture() = GrpcUpdateAccount(
    name = KeyGenerator.generate(KeyLength.Medium),
  )
