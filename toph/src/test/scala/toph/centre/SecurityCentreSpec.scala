package toph.centre

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import toph.TophSpec
import toph.model.ProtoAccountFixture
import toph.security.MultiOpenIdProvider
import toph.service.AccountService
import toph.service.TokenService
import zio.ZIO
import zio.ZLayer
import zio.test.*
import one.estrondo.sweetmockito.zio.given
import toph.security.AuthorisedAccessFixture

object SecurityCentreSpec extends TophSpec:

  def spec = suite("The SecurityCentre")(
    test("It should reject an invalid token.") {
      val securityContext = SecurityCentreFixture.createRandomContext()
      for
        _ <- SweetMockitoLayer[MultiOpenIdProvider]
               .whenF2(_.validate("a-token", "a-provider"))
               .thenReturn(None)

        token <- ZIO.serviceWithZIO[SecurityCentre](_.authoriseOpenId("a-token", "a-provider", securityContext))
      yield assertTrue(token.isEmpty)
    },
    test("It should capture any error during OpenId validation.") {
      val securityContext = SecurityCentreFixture.createRandomContext()
      for
        _ <- SweetMockitoLayer[MultiOpenIdProvider]
               .whenF2(_.validate("t", "p"))
               .thenFail(IllegalStateException("@@@"))

        exit <- ZIO.serviceWithZIO[SecurityCentre](_.authoriseOpenId("t", "p", securityContext)).exit
      yield assertTrue(exit.is(_.failure).getMessage == "Unable to validate the oidc token!")
    },
    test("It should accept a valid token.") {

      val securityContext        = SecurityCentreFixture.createRandomContext()
      val expectedCreatedAccount = AuthorisedAccessFixture.createRandom()
      val expectedProtoAccount   = ProtoAccountFixture
        .createRandom()
        .copy(
          name = Some(expectedCreatedAccount.account.name),
        )
      val expectedCreatedToken   = AuthorisedAccessFixture.createRandom()

      // noinspection OptionEqualsSome
      for
        _ <- SweetMockitoLayer[MultiOpenIdProvider]
               .whenF2(_.validate("t", "p"))
               .thenReturn(Some((expectedCreatedAccount.account.email, expectedProtoAccount)))
        _ <- SweetMockitoLayer[AccountService]
               .whenF2(_.findOrCreate(expectedCreatedAccount.account.email, expectedProtoAccount))
               .thenReturn(expectedCreatedAccount.account)
        _ <-
          SweetMockitoLayer[TokenService]
            .whenF2(_.authorise(expectedCreatedAccount.account, securityContext.device, securityContext.origin))
            .thenReturn(expectedCreatedToken)

        token <- ZIO.serviceWithZIO[SecurityCentre](_.authoriseOpenId("t", "p", securityContext))
      yield assertTrue(token == Some(expectedCreatedToken))
    },
  ).provideSome(
    SweetMockitoLayer.newMockLayer[MultiOpenIdProvider],
    SweetMockitoLayer.newMockLayer[AccountService],
    SweetMockitoLayer.newMockLayer[TokenService],
    ZLayer.fromFunction(SecurityCentre.apply),
  )
