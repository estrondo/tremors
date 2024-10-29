package toph.grpc.impl

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

        case GRPCOpenIdTokenAuthorisationRequest(provider, token, _) =>
          authoriseOpenId(token, provider)

    private def authoriseOpenId(token: String, provider: String): IO[StatusException, GRPCAuthorisationResponse] =
      securityCentre
        .authorise(token, provider)
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
      ZIO.succeed(GRPCAuthorisationResponse(token = token.token))
