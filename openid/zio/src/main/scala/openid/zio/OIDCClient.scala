package openid.zio

import pdi.jwt.JwtClaim
import pdi.jwt.JwtZIOJson
import zio.RIO
import zio.Task
import zio.ZIO
import zio.http.Body
import zio.http.Client
import zio.http.Request
import zio.http.Response
import zio.http.URL
import zio.http.model.Headers
import zio.http.model.Status
import zio.json.ast.Json

import java.nio.charset.Charset
import java.security.PublicKey
import javax.crypto.SecretKey
import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait OIDCClient:

  def userInfo(accessToken: AccessToken): RIO[Client, Json]

object OIDCClient:

  def apply(oip: OIP): RIO[Client, OIDCClient] =
    ZIO.succeed(new Impl(oip))

  private class Impl(oip: OIP) extends OIDCClient:

    override def userInfo(accessToken: AccessToken): RIO[Client, Json] =
      for
        url    <- parseUrl(oip.userInfoEndpoint)
                    .mapError(IllegalArgumentException("Invalid UserInfoEndpoint!", _))
                    .someOrFail(IllegalStateException("There is no issuerEndpoint!"))
        claims <- issueUserInfoEndpoint(url, accessToken)
      yield claims

    private def createGetRequest(url: URL, accessToken: AccessToken): Task[Request] = ZIO.succeed(
      Request
        .get(url)
        .addHeaders(Headers.bearerAuthorizationHeader(accessToken.token))
    )

    private def decode(token: String): Try[Json] =
      oip.key match
        case secretKey: SecretKey => JwtZIOJson.decodeJson(token, secretKey)
        case publicKey: PublicKey => JwtZIOJson.decodeJson(token, publicKey)

    private def issueUserInfoEndpoint(url: URL, accessToken: AccessToken): RIO[Client, Json] =
      for
        request  <- createGetRequest(url, accessToken)
        response <- Client.request(request)
        claims   <- response.status match
                      case Status.Ok =>
                        decodeIdToken(response.bearerToken)
                          .someOrFail(IllegalStateException("There is no IdToken in UserInfoEndpoint response!"))
                      case status    =>
                        ZIO.fail(IllegalStateException(s"Invalid OIP Response Status ${status.code}: ${status.text}."))
      yield claims

    private def decodeIdToken(opt: Option[String]): Task[Option[Json]] = ZIO.fromTry {
      opt match
        case Some(token) => decode(token).map(Some(_))
        case None        => Success(None)
    }

    private def parseUrl(opt: Option[String]): Task[Option[URL]] = ZIO.fromEither {
      opt.map(URL.fromString) match
        case Some(either) => either.map(Some(_))
        case None         => Right(None)

    }
