package openid.zio

import one.estrondo.sweetmockito.Answer
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import pdi.jwt.JwtZIOJson
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.http.Client
import zio.http.Request
import zio.http.Response
import zio.http.TestClient
import zio.http.URL
import zio.http.model.Headers
import zio.http.netty.ChannelFactories.Client
import zio.json.ast.Json
import zio.json.given
import zio.test.TestEnvironment
import zio.test.assertTrue

import java.security.KeyPair
import java.security.PrivateKey

object OIDCClientSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("An OIDCClient")(
      test("It should issuer an UserInfo Request") {

        for
          keyPair       <- ZIO.service[KeyPair]
          idTokenHeader <- ZIO.fromEither("""{"typ": "JWT","alg":"RS512"}""".fromJson[Json])
          idTokenClaims <- ZIO.fromEither("""{"sub":"A super owner!", "email": "tupac@e.f.g"}""".fromJson[Json])
          idToken        = JwtZIOJson.encode(idTokenHeader, idTokenClaims, keyPair.getPrivate())
          endpoint      <- ZIO.fromEither(URL.fromString("http://localhost/userinfo"))
          request        = Request
                             .get(endpoint)
                             .addHeaders(Headers.bearerAuthorizationHeader("a.b.c"))
          response       = Response.ok
                             .addHeaders(Headers.bearerAuthorizationHeader(idToken))

          _      <- TestClient.addRequestResponse(request, response)
          claims <- ZIO.serviceWithZIO[OIDCClient](_.userInfo(AccessToken("a.b.c")))
        yield assertTrue(claims == Json.Obj("sub" -> Json.Str("A super owner!"), "email" -> Json.Str("tupac@e.f.g")))
      }
    ).provideSome[Scope](
      TestClient.layer,
      ZLayer.succeed(keyPairGenerator()),
      ZLayer.fromZIO(
        ZIO.serviceWithZIO[KeyPair](keyPair =>
          OIDCClient(
            OIP(
              key = keyPair.getPublic(),
              userInfoEndpoint = Some("http://localhost/userinfo")
            )
          )
        )
      )
    )
