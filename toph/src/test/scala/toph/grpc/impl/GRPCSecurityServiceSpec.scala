package toph.grpc.impl

import io.grpc.Status
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import scalapb.zio_grpc.RequestContext
import toph.TophException
import toph.TophSpec
import toph.centre.SecurityCentre
import toph.grpc.GRPCOpenIdTokenAuthorisationRequest
import toph.grpc.ZioGrpc
import toph.model.AccountFixture
import toph.security.Token
import zio.ZIO
import zio.ZLayer
import zio.test.*
import zio.test.given

object GRPCSecurityServiceSpec extends TophSpec:

  override def spec = suite("The GRPCSecurityService")(
    test("It should authorise a valid openid token.") {

      val expectedToken = Token(
        account = AccountFixture.createRandom(),
        token = "my-token",
      )

      for
        _ <- SweetMockitoLayer[SecurityCentre]
               .whenF2(_.authorise("some-token", "some-provider"))
               .thenReturn(Some(expectedToken))

        response <- ZIO.serviceWithZIO[ZioGrpc.ZSecurityService[RequestContext]](
                      _.authorise(
                        GRPCOpenIdTokenAuthorisationRequest(
                          provider = "some-provider",
                          token = "some-token",
                        ),
                        null,
                      ),
                    )
      yield assertTrue(
        response.token == expectedToken.token,
      )
    },
    test("It should reject an expected token.") {
      for
        _ <- SweetMockitoLayer[SecurityCentre]
               .whenF2(_.authorise("some-token", "some-provider"))
               .thenReturn(None)

        exit <- ZIO
                  .serviceWithZIO[ZioGrpc.ZSecurityService[RequestContext]](
                    _.authorise(
                      GRPCOpenIdTokenAuthorisationRequest(
                        provider = "some-provider",
                        token = "some-token",
                      ),
                      null,
                    ),
                  )
                  .exit
      yield assertTrue(
        exit.is(_.failure).getStatus == Status.UNAUTHENTICATED,
      )
    },
    test("It should return any error as unauthenticated.") {
      for
        _ <- SweetMockitoLayer[SecurityCentre]
               .whenF2(_.authorise("some-token", "some-provider"))
               .thenFail(TophException.Security("@@@"))

        exit <- ZIO
                  .serviceWithZIO[ZioGrpc.ZSecurityService[RequestContext]](
                    _.authorise(
                      GRPCOpenIdTokenAuthorisationRequest(
                        provider = "some-provider",
                        token = "some-token",
                      ),
                      null,
                    ),
                  )
                  .exit
      yield assertTrue(
        exit.is(_.failure).getStatus == Status.UNAUTHENTICATED,
      )
    },
  ).provideSome(
    SweetMockitoLayer.newMockLayer[SecurityCentre],
    ZLayer(ZIO.serviceWithZIO((securityCentre: SecurityCentre) => GRPCSecurityService(securityCentre))),
  )
