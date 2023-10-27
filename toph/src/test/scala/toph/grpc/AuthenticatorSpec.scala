package toph.grpc

import io.grpc.Attributes
import io.grpc.StatusException
import io.grpc.testing.TestMethodDescriptors
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import scalapb.zio_grpc.RequestContext
import scalapb.zio_grpc.SafeMetadata
import toph.TophSpec
import toph.model.AuthenticatedUserFixture
import toph.security.TokenService
import zio.ZIO
import zio.ZLayer
import zio.test.Assertion
import zio.test.assert
import zio.test.assertTrue

object AuthenticatorSpec extends TophSpec:

  private def createRequest(token: String) =
    for
      metadata <- SafeMetadata.make(
                    "authorization" -> s"Bearer $token"
                  )
      empty    <- SafeMetadata.make
    yield RequestContext(metadata, empty, None, TestMethodDescriptors.voidMethod(), Attributes.EMPTY)

  private def createRequest() =
    for
      metadata <- SafeMetadata.make
      empty    <- SafeMetadata.make
    yield RequestContext(metadata, empty, None, TestMethodDescriptors.voidMethod(), Attributes.EMPTY)

  override def spec = suite("A AuthenticatorSpec")(
    test("It should extract claims from a valid request.") {
      val expectedUser = AuthenticatedUserFixture.createRandom()
      for
        context           <- createRequest(expectedUser.token)
        _                 <- SweetMockitoLayer[TokenService]
                               .whenF2(_.decode(expectedUser.token))
                               .thenReturn(Some(expectedUser.claims))
        authenticatedUser <- ZIO.serviceWithZIO[Authenticator](_.authenticate(context))
      yield assertTrue(
        authenticatedUser == expectedUser
      )
    },
    test("It should reject an invalid token.") {
      val expectedToken = "aaaaaa"
      for
        context <- createRequest(expectedToken)
        exit    <- ZIO.serviceWithZIO[Authenticator](_.authenticate(context)).exit
      yield assert(exit)(Assertion.failsWithA[StatusException])
    },
    test("It should reject an authorized request.") {
      for
        context <- createRequest()
        exit    <- ZIO.serviceWithZIO[Authenticator](_.authenticate(context)).exit
      yield assert(exit)(Assertion.failsWithA[StatusException])
    }
  ).provideSome(
    SweetMockitoLayer.newMockLayer[TokenService],
    ZLayer {
      ZIO.serviceWithZIO { (service: TokenService) =>
        Authenticator(service)
      }
    }
  )
