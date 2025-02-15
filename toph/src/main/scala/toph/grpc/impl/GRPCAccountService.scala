package toph.grpc.impl

import io.grpc.Status
import io.grpc.StatusException
import toph.context.TophExecutionContext
import toph.model.Account
import toph.security.AccessToken
import toph.service.AccountService
import toph.v1.grpc.GrpcAccount
import toph.v1.grpc.GrpcUpdateAccount
import toph.v1.grpc.ZioGrpc
import zio.Cause
import zio.IO
import zio.Task
import zio.UIO
import zio.ZIO

object GRPCAccountService:

  def apply(accountService: AccountService): Task[ZioGrpc.ZAccountService[AccessToken]] =
    ZIO.succeed(Impl(accountService))

  private class Impl(accountService: AccountService) extends ZioGrpc.ZAccountService[AccessToken]:

    override def update(request: GrpcUpdateAccount, token: AccessToken): IO[StatusException, GrpcAccount] =
      for
        updatedAccount <- accountService
                            .update(token.account.key, AccountService.Update(request.name))(using
                              TophExecutionContext.account(token.account),
                            )
                            .flatMapError(handleUpdateError(token))
        transformed    <- GRPCAccountTransformer
                            .from(updatedAccount)
                            .flatMapError(handleTransformUserError(updatedAccount))
      yield transformed

    private def handleUpdateError(token: AccessToken)(cause: Throwable): UIO[StatusException] =
      ZIO.logErrorCause(
        s"An error happened during updating of user=${token.account.key}!",
        Cause.die(cause),
      ) as StatusException(Status.INTERNAL)

    private def handleTransformUserError(account: Account)(cause: Throwable): UIO[StatusException] =
      ZIO.logErrorCause(
        s"An error happened during transformation of Account ${account.key}.",
        Cause.die(cause),
      ) as StatusException(Status.INTERNAL)
