package toph.grpc

import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusException
import scalapb.zio_grpc.RequestContext
import toph.model.AuthenticatedUser
import toph.security.TokenService
import zio.Cause
import zio.IO
import zio.Task
import zio.ZIO

trait Authenticator:

  def authenticate(request: RequestContext): IO[StatusException, AuthenticatedUser]

object Authenticator:

  def apply(tokenService: TokenService): Task[Authenticator] =
    ZIO.attempt(Impl(tokenService))

  private class Impl(tokenService: TokenService) extends Authenticator:

    private val extractToken =
      val base64 = """[a-zA-Z0-9+/]+={0,3}"""
      s"^Bearer\\s($base64\\.$base64\\.$base64)$$".r

    private val authorizationHeader = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)

    override def authenticate(request: RequestContext): IO[StatusException, AuthenticatedUser] =
      request.metadata.get(authorizationHeader).flatMap {
        case Some(extractToken(token)) =>
          tokenService
            .decode(token)
            .flatMapError { error =>
              ZIO.logWarningCause("It was impossible to decode token!", Cause.die(error)) *>
                ZIO.succeed(StatusException(Status.UNAUTHENTICATED))
            }
            .flatMap {
              case Some(claims) => ZIO.succeed(AuthenticatedUser(token, claims))
              case None         => ZIO.fail(StatusException(Status.UNAUTHENTICATED))
            }
        case _                         =>
          ZIO.fail(StatusException(Status.UNAUTHENTICATED))
      }
