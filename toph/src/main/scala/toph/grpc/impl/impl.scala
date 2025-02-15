package toph.grpc.impl

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusException
import scalapb.UnknownFieldSet
import scalapb.zio_grpc.RequestContext
import toph.model.Account
import toph.security.AccessToken
import toph.security.TokenCodec
import toph.v1.grpc.GrpcAccount
import zio.Exit
import zio.IO
import zio.Task
import zio.ZIO

//noinspection ScalaFileName
object GRPCAccountTransformer:

  def from(account: Account): Task[GrpcAccount] = ZIO.attempt {
    account
      .into[GrpcAccount]
      .transform(Field.const(_.unknownFields, UnknownFieldSet.empty))
  }

val tokenMetadataKey = Metadata.Key.of("token-bin", Metadata.BINARY_BYTE_MARSHALLER)

val unauthorized = Exit.fail(StatusException(Status.UNAUTHENTICATED))

def convertRequestContextToToken(tokenCodec: TokenCodec)(request: RequestContext): IO[StatusException, AccessToken] =
  request.metadata.get(tokenMetadataKey).flatMap {
    case Some(bytes) =>
      tokenCodec
        .decode(bytes, ???)
        .foldCauseZIO(
          failure = cause => ZIO.logWarningCause("Unable to decode token!", cause) *> unauthorized,
          success = { token => Exit.succeed(???) },
        )
    case None        =>
      unauthorized
  }
