package toph.grpc.impl

import com.google.protobuf.ByteString
import io.grpc.Status
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers
import scalapb.zio_grpc.RequestContext
import toph.TophException
import toph.TophSpec
import toph.centre.SecurityCentre
import toph.model.AccountFixture
import toph.security.Token
import toph.v1.grpc.GrpcOpenIdTokenAuthorisationRequest
import toph.v1.grpc.ZioGrpc
import zio.ZIO
import zio.ZLayer
import zio.test.*

object GRPCSecurityServiceSpec extends TophSpec:

  override def spec = suite("The GRPCSecurityService")(
    test("It should authorise a valid openid token.") {

      val expectedToken = Token(
        account = AccountFixture.createRandom(),
        token = "my-token".getBytes,
      )

      for
        _ <- SweetMockitoLayer[SecurityCentre]
               .whenF2(
                 _.authoriseOpenId(ArgumentMatchers.eq("some-token"), ArgumentMatchers.eq("some-provider"))(using
                   ArgumentMatchers.any(),
                 ),
               )
               .thenReturn(Some(expectedToken))

        response <- ZIO.serviceWithZIO[ZioGrpc.ZSecurityService[RequestContext]](
                      _.authorise(
                        GrpcOpenIdTokenAuthorisationRequest(
                          provider = "some-provider",
                          token = "some-token",
                        ),
                        null,
                      ),
                    )
      yield assertTrue(
        response.token == ByteString.copyFrom(expectedToken.token),
      )
    },
    test("It should reject an expected token.") {
      for
        _ <- SweetMockitoLayer[SecurityCentre]
               .whenF2(
                 _.authoriseOpenId(
                   org.mockito.ArgumentMatchers.eq("some-token"),
                   org.mockito.ArgumentMatchers.eq("some-provider"),
                 )(using ArgumentMatchers.any()),
               )
               .thenReturn(None)

        exit <- ZIO
                  .serviceWithZIO[ZioGrpc.ZSecurityService[RequestContext]](
                    _.authorise(
                      GrpcOpenIdTokenAuthorisationRequest(
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
               .whenF2(
                 _.authoriseOpenId(
                   org.mockito.ArgumentMatchers.eq("some-token"),
                   org.mockito.ArgumentMatchers.eq("some-provider"),
                 )(using ArgumentMatchers.any()),
               )
               .thenFail(TophException.Security("@@@"))

        exit <- ZIO
                  .serviceWithZIO[ZioGrpc.ZSecurityService[RequestContext]](
                    _.authorise(
                      GrpcOpenIdTokenAuthorisationRequest(
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
