package toph.grpc.impl

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusException
import scalapb.UnknownFieldSet
import scalapb.zio_grpc.RequestContext
import toph.grpc.GRPCAccount
import toph.model.Account
import toph.security.Token
import toph.security.TokenService
import zio.Exit
import zio.IO
import zio.Task
import zio.ZIO

//noinspection ScalaFileName
object GRPCAccountTransformer:

  def from(account: Account): Task[GRPCAccount] = ZIO.attempt {
    account
      .into[GRPCAccount]
      .transform(Field.const(_.unknownFields, UnknownFieldSet.empty))
  }

val tokenMetadataKey = Metadata.Key.of("token-bin", Metadata.BINARY_BYTE_MARSHALLER)

val unauthorized = Exit.fail(StatusException(Status.UNAUTHENTICATED))

def convertRequestContextToToken(tokenService: TokenService)(request: RequestContext): IO[StatusException, Token] =
  request.metadata.get(tokenMetadataKey).flatMap {
    case Some(bytes) =>
      tokenService
        .decode(bytes)
        .foldCauseZIO(
          failure = cause => ZIO.logWarningCause("Unable to decode token!", cause) *> unauthorized,
          success = {
            case Some(token) => Exit.succeed(token)
            case None        => unauthorized
          },
        )
    case None        =>
      unauthorized
  }
