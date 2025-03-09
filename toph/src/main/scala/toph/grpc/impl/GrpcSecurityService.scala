package toph.grpc.impl

import com.google.protobuf.ByteString
import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusException
import scalapb.zio_grpc.RequestContext
import toph.centre.SecurityCentre
import toph.context.TophExecutionContext
import toph.security.AuthorisedAccess
import toph.v1.grpc.GrpcAuthorisationRequest
import toph.v1.grpc.GrpcAuthorisationResponse
import toph.v1.grpc.GrpcOpenIdTokenAuthorisationRequest
import toph.v1.grpc.GrpcRefreshAuthorisation
import toph.v1.grpc.ZioGrpc
import toph.v1.grpc.ZioGrpc.ZSecurityService
import zio.IO
import zio.UIO
import zio.ZIO
import zio.ZIOAspect

object GrpcSecurityService:

  def apply(securityCentre: SecurityCentre): UIO[ZioGrpc.ZSecurityService[RequestContext]] =
    ZIO.succeed(Impl(securityCentre))

  private val OriginKey = Metadata.Key.of("x-forwarded-for", Metadata.ASCII_STRING_MARSHALLER)

  private def createSecurityContext(
      device: String,
      context: RequestContext,
  ): UIO[SecurityCentre.Context] =
    for origin <- context.metadata.get(OriginKey)
    yield SecurityCentre.Context(
      device = device,
      origin = origin,
    )

  class Impl(securityCentre: SecurityCentre) extends ZioGrpc.ZSecurityService[RequestContext]:

    override def authorise(
        request: GrpcAuthorisationRequest,
        context: RequestContext,
    ): IO[StatusException, GrpcAuthorisationResponse] =
      request match
        case GrpcAuthorisationRequest.Empty =>
          ZIO.logWarning("Empty request.") *> ZIO.fail(StatusException(Status.UNAUTHENTICATED))

        case GrpcOpenIdTokenAuthorisationRequest(provider, token, device, _) =>
          createSecurityContext(device, context)
            .flatMap(authoriseOpenId(token, provider, _))

    private def authoriseOpenId(
        token: String,
        provider: String,
        securityContext: SecurityCentre.Context,
    ): IO[StatusException, GrpcAuthorisationResponse] =
      securityCentre
        .authoriseOpenId(token, provider, securityContext)(using TophExecutionContext.system[ZSecurityService[?]])
        .foldCauseZIO(
          failure = cause =>
            ZIO.logErrorCause("Unable to authorise!", cause) *> ZIO.fail(StatusException(Status.UNAUTHENTICATED)),
          success =
            case Some(authorisedAccess) => convertFrom(authorisedAccess)
            case None                   => ZIO.logError("No authorisedAccess!") *> ZIO.fail(StatusException(Status.UNAUTHENTICATED)),
        ) @@ ZIOAspect.annotated(
        "token"    -> token,
        "provider" -> provider,
      )

    override def refresh(
        request: GrpcRefreshAuthorisation,
        context: RequestContext,
    ): IO[StatusException, GrpcAuthorisationResponse] = ???

    private def convertFrom(createdToken: AuthorisedAccess): UIO[GrpcAuthorisationResponse] =
      ZIO.succeed(
        GrpcAuthorisationResponse(
          accessToken = ByteString.copyFrom(createdToken.accessToken),
          refreshToken = createdToken.refreshToken,
        ),
      )
