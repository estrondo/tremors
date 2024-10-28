package toph.grpc.impl

import io.grpc.Status
import io.grpc.StatusException
import toph.context.TophExecutionContext
import toph.grpc.GRPCAccount
import toph.grpc.GRPCUpdateAccount
import toph.grpc.ZioGrpc
import toph.model.Account
import toph.security.Token
import toph.service.AccountService
import zio.Cause
import zio.IO
import zio.Task
import zio.UIO
import zio.ZIO

object GRPCAccountService:

  def apply(accountService: AccountService): Task[ZioGrpc.ZAccountService[Token]] =
    ZIO.succeed(Impl(accountService))

  private class Impl(accountService: AccountService) extends ZioGrpc.ZAccountService[Token]:

    override def update(request: GRPCUpdateAccount, token: Token): IO[StatusException, GRPCAccount] =
      for
        updatedAccount <- accountService
                            .update(token.account.key, AccountService.Update(request.name))(using
                              TophExecutionContext.identifiedAccount(token.account),
                            )
                            .flatMapError(handleUpdateError(token))
        transformed    <- GRPCAccountTransformer
                            .from(updatedAccount)
                            .flatMapError(handleTransformUserError(updatedAccount))
      yield transformed

    private def handleUpdateError(token: Token)(cause: Throwable): UIO[StatusException] =
      ZIO.logErrorCause(
        s"An error happened during updating of user=${token.account.key}!",
        Cause.die(cause),
      ) as StatusException(Status.INTERNAL)

    private def handleTransformUserError(account: Account)(cause: Throwable): UIO[StatusException] =
      ZIO.logErrorCause(
        s"An error happened during transformation of Account ${account.key}.",
        Cause.die(cause),
      ) as StatusException(Status.INTERNAL)
