package toph.grpc.impl

import com.google.protobuf.ByteString
import io.grpc.Status
import io.grpc.StatusException
import scalapb.zio_grpc.RequestContext
import toph.centre.SecurityCentre
import toph.context.TophExecutionContext
import toph.security.Token
import toph.v1.grpc.GrpcAuthorisationRequest
import toph.v1.grpc.GrpcAuthorisationResponse
import toph.v1.grpc.GrpcOpenIdTokenAuthorisationRequest
import toph.v1.grpc.ZioGrpc
import toph.v1.grpc.ZioGrpc.ZSecurityService
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
        request: GrpcAuthorisationRequest,
        context: RequestContext,
    ): IO[StatusException, GrpcAuthorisationResponse] =
      request match
        case GrpcAuthorisationRequest.Empty =>
          ZIO.logWarning("Empty request.") *> ZIO.fail(StatusException(Status.UNAUTHENTICATED))

        case GrpcOpenIdTokenAuthorisationRequest(provider, token, _) =>
          authoriseOpenId(token, provider)

    private def authoriseOpenId(token: String, provider: String): IO[StatusException, GrpcAuthorisationResponse] =
      securityCentre
        .authoriseOpenId(token, provider)(using TophExecutionContext.system[ZSecurityService[_]])
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

    private def convertFrom(token: Token): UIO[GrpcAuthorisationResponse] =
      ZIO.succeed(GrpcAuthorisationResponse(token = ByteString.copyFrom(token.token)))
