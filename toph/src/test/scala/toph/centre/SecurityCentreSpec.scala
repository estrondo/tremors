package toph.centre

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import toph.TophSpec
import toph.security.MultiOpenIdProvider
import toph.security.Token
import toph.security.TokenFixture
import toph.security.TokenService
import zio.ZIO
import zio.ZLayer
import zio.test.*

object SecurityCentreSpec extends TophSpec:

  def spec = suite("The SecurityCentre")(
    test("It should reject an invalid token.") {
      for
        _ <- SweetMockitoLayer[MultiOpenIdProvider]
               .whenF2(_.validate("a-token", "a-provider"))
               .thenReturn(None)

        token <- ZIO.serviceWithZIO[SecurityCentre](_.authorise("a-token", "a-provider"))
      yield assertTrue(token.isEmpty)
    },
    test("It should capture any error during OpenId validation.") {
      for
        _ <- SweetMockitoLayer[MultiOpenIdProvider]
               .whenF2(_.validate("t", "p"))
               .thenFail(IllegalStateException("@@@"))

        exit <- ZIO.serviceWithZIO[SecurityCentre](_.authorise("t", "p")).exit
      yield assertTrue(exit.is(_.failure).getMessage == "Unable to validate!")
    },
    test("It should accept a valid token.") {

      val expectedToken = TokenFixture.createRandom()
      for
        _ <- SweetMockitoLayer[MultiOpenIdProvider]
               .whenF2(_.validate("t", "p"))
               .thenReturn(Some(expectedToken.account.email))
        _ <- SweetMockitoLayer[AccountService]
               .whenF2(_.findOrCreate(expectedToken.account.email))
               .thenReturn(expectedToken.account)
        _ <- SweetMockitoLayer[TokenService]
               .whenF2(_.encode(expectedToken.account))
               .thenReturn(expectedToken)

        token <- ZIO.serviceWithZIO[SecurityCentre](_.authorise("t", "p"))
      yield assertTrue(token.contains(expectedToken))
    },
  ).provideSome(
    SweetMockitoLayer.newMockLayer[MultiOpenIdProvider],
    SweetMockitoLayer.newMockLayer[AccountService],
    SweetMockitoLayer.newMockLayer[TokenService],
    ZLayer.fromFunction(SecurityCentre.apply),
  )
