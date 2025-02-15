package toph.grpc

import com.google.protobuf.ByteString
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers
import scalapb.zio_grpc.RequestContext
import scalapb.zio_grpc.Server
import toph.centre.SecurityCentre
import toph.grpc.impl.GRPCSecurityService
import toph.model.AccountFixture
import toph.security.Token
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

      val expectedToken = Token(
        account = AccountFixture.createRandom(),
        token = "aaaabbbbbbcccc".getBytes(),
      )

      for
        _        <- SweetMockitoLayer[SecurityCentre]
                      .whenF2(
                        _.authoriseOpenId(ArgumentMatchers.eq(request.token), ArgumentMatchers.eq(request.provider))(using
                          ArgumentMatchers.any(),
                        ),
                      )
                      .thenReturn(Some(expectedToken))
        response <- SecurityServiceClient.authorise(request)
      yield assertTrue(
        response.token == ByteString.copyFrom(expectedToken.token),
      )
    },
  ).provideSome[Scope](
    SweetMockitoLayer.newMockLayer[SecurityCentre],
    GrpcTestableService.serverOf[ZioGrpc.ZSecurityService[RequestContext]],
    GrpcTestableService.clientOf(ZioGrpc.SecurityServiceClient.live(_)),
    ZLayer.fromZIO(ZIO.serviceWithZIO(GRPCSecurityService.apply)),
  )
