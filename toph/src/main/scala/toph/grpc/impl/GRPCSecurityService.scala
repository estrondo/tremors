package toph.grpc.impl

import com.google.protobuf.ByteString
import io.grpc.Status
import io.grpc.StatusException
import scalapb.zio_grpc.RequestContext
import toph.centre.SecurityCentre
import toph.grpc.GRPCAuthorisationRequest
import toph.grpc.GRPCAuthorisationResponse
import toph.grpc.GRPCOpenIdTokenAuthorisationRequest
import toph.grpc.ZioGrpc
import toph.security.Token
import zio.Cause
import zio.IO
import zio.UIO
import zio.ZIO

object GRPCSecurityService:

  val Version = "1"

  def apply(securityCentre: SecurityCentre): UIO[ZioGrpc.ZSecurityService[RequestContext]] =
    ZIO.succeed(Impl(securityCentre))

  private class Impl(securityCentre: SecurityCentre) extends ZioGrpc.ZSecurityService[RequestContext]:

    override def authorise(
        request: GRPCAuthorisationRequest,
        context: RequestContext,
    ): IO[StatusException, GRPCAuthorisationResponse] =
      request match
        case GRPCAuthorisationRequest.Empty =>
          ZIO.logWarning("Empty request.") *> ZIO.fail(StatusException(Status.UNAUTHENTICATED))

        case GRPCOpenIdTokenAuthorisationRequest(Version, provider, token, _) =>
          authoriseOpenId(token, provider)

        case GRPCOpenIdTokenAuthorisationRequest(version, _, _, _) =>
          ZIO.logWarning(s"Invalid request version: $version!") *> ZIO.fail(StatusException(Status.UNAUTHENTICATED))

    private def authoriseOpenId(token: String, provider: String): IO[StatusException, GRPCAuthorisationResponse] =
      securityCentre
        .authoriseOpenId(token, provider)
        .flatMap {
          case Some(token) => convertFrom(token)
          case None        => ZIO.fail(StatusException(Status.UNAUTHENTICATED))
        }
        .flatMapError[Any, StatusException] {
          case statusException: StatusException =>
            ZIO.succeed(statusException)
          case other                            =>
            ZIO.logWarningCause("Unable to authorise.", Cause.die(other)) *> ZIO.succeed(
              StatusException(Status.UNAUTHENTICATED),
            )
        }

    private def convertFrom(token: Token): UIO[GRPCAuthorisationResponse] =
      ZIO.succeed(GRPCAuthorisationResponse(version = Version, token = ByteString.copyFrom(token.token)))
