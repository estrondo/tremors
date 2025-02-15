package toph.grpc.impl

import com.google.protobuf.ByteString
import io.grpc.MethodDescriptor
import io.grpc.Status
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers
import scalapb.zio_grpc.RequestContext
import toph.TophException
import toph.TophSpec
import toph.centre.SecurityCentre
import toph.centre.SecurityCentreFixture
import toph.security.AuthorisedAccessFixture
import toph.v1.grpc.GrpcOpenIdTokenAuthorisationRequest
import toph.v1.grpc.SecurityServiceGrpc
import toph.v1.grpc.ZioGrpc
import tremors.zio.grpc.RequestContextFixture
import zio.ZIO
import zio.ZLayer
import zio.test.*

object GrpcSecurityServiceSpec extends TophSpec:

  private def createRequestContext[A, B](methodDescriptor: MethodDescriptor[A, B]) =
    RequestContextFixture.createRandom(
      methodDescriptor,
      Seq("x-forwarded-for" -> "127.0.0.1"),
    )

  override def spec = suite("The GRPCSecurityService")(
    test("It should authorise a valid openid token.") {

      val expectedToken   = AuthorisedAccessFixture.createRandom()
      val securityContext = SecurityCentreFixture
        .createRandomContext()
        .copy(origin = Some("127.0.0.1"))

      for
        _ <- SweetMockitoLayer[SecurityCentre]
               .whenF2(
                 _.authoriseOpenId(
                   ArgumentMatchers.eq("some-token"),
                   ArgumentMatchers.eq("some-provider"),
                   ArgumentMatchers.eq(securityContext),
                 )(using
                   ArgumentMatchers.any(),
                 ),
               )
               .thenReturn(Some(expectedToken))

        requestContext <- createRequestContext(SecurityServiceGrpc.METHOD_AUTHORISE)
        response       <- ZIO.serviceWithZIO[ZioGrpc.ZSecurityService[RequestContext]](
                            _.authorise(
                              GrpcOpenIdTokenAuthorisationRequest(
                                provider = "some-provider",
                                token = "some-token",
                                device = securityContext.device,
                              ),
                              requestContext,
                            ),
                          )
      yield assertTrue(
        response.accessToken == ByteString.copyFrom(expectedToken.accessToken),
      )
    },
    test("It should reject an expected token.") {
      for
        _ <- SweetMockitoLayer[SecurityCentre]
               .whenF2(
                 _.authoriseOpenId(
                   ArgumentMatchers.eq("some-token"),
                   ArgumentMatchers.eq("some-provider"),
                   ArgumentMatchers.any(),
                 )(using ArgumentMatchers.any()),
               )
               .thenReturn(None)

        requestContext <- createRequestContext(SecurityServiceGrpc.METHOD_AUTHORISE)
        exit           <- ZIO
                            .serviceWithZIO[ZioGrpc.ZSecurityService[RequestContext]](
                              _.authorise(
                                GrpcOpenIdTokenAuthorisationRequest(
                                  provider = "some-provider",
                                  token = "some-token",
                                ),
                                requestContext,
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
                   ArgumentMatchers.eq("some-token"),
                   ArgumentMatchers.eq("some-provider"),
                   ArgumentMatchers.any(),
                 )(using ArgumentMatchers.any()),
               )
               .thenFail(TophException.Security("@@@"))

        request <- RequestContextFixture.createRandom(SecurityServiceGrpc.METHOD_AUTHORISE)
        exit    <- ZIO
                     .serviceWithZIO[ZioGrpc.ZSecurityService[RequestContext]](
                       _.authorise(
                         GrpcOpenIdTokenAuthorisationRequest(
                           provider = "some-provider",
                           token = "some-token",
                         ),
                         request,
                       ),
                     )
                     .exit
      yield assertTrue(
        exit.is(_.failure).getStatus == Status.UNAUTHENTICATED,
      )
    },
  ).provideSome(
    SweetMockitoLayer.newMockLayer[SecurityCentre],
    ZLayer(ZIO.serviceWithZIO((securityCentre: SecurityCentre) => GrpcSecurityService(securityCentre))),
  ) @@ TestAspect.sequential
