package toph.grpc

import com.google.protobuf.ByteString
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers
import scalapb.zio_grpc.RequestContext
import scalapb.zio_grpc.Server
import toph.centre.SecurityCentre
import toph.grpc.impl.GrpcSecurityService
import toph.security.AuthorisedAccessFixture
import toph.v1.grpc.GrpcOpenIdTokenAuthorisationRequest
import toph.v1.grpc.ZioGrpc
import toph.v1.grpc.ZioGrpc.SecurityServiceClient
import tremors.zio.grpc.GrpcTestableService
import zio.RLayer
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object GrpcSecurityServiceSpec extends TophGrpcSpec:

  override def spec = suite("The SecurityService")(
    test("It should create a toph-token for a valid Open Id Token") {

      val request = GrpcOpenIdTokenAuthorisationRequest(
        provider = "wechat",
        token = "aaaaa",
      )

      val expectedToken = AuthorisedAccessFixture.createRandom()

      for
        _        <- SweetMockitoLayer[SecurityCentre]
                      .whenF2(
                        _.authoriseOpenId(
                          ArgumentMatchers.eq(request.token),
                          ArgumentMatchers.eq(request.provider),
                          ArgumentMatchers.any(),
                        )(using
                          ArgumentMatchers.any(),
                        ),
                      )
                      .thenReturn(Some(expectedToken))
        response <- SecurityServiceClient.authorise(request)
      yield assertTrue(
        response.accessToken == ByteString.copyFrom(expectedToken.accessToken),
      )
    },
  ).provideSome[Scope](
    SweetMockitoLayer.newMockLayer[SecurityCentre],
    GrpcTestableService.serverOf[ZioGrpc.ZSecurityService[RequestContext]],
    GrpcTestableService.clientOf(ZioGrpc.SecurityServiceClient.live(_)),
    ZLayer.fromZIO(ZIO.serviceWithZIO(GrpcSecurityService.apply)),
  )
