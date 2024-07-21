package toph.grpc

import io.grpc.Status
import io.grpc.StatusException
import toph.centre.AccountService
import toph.context.TophExecutionContext
import toph.model.Account
import toph.security.Token
import toph.service.UpdateUser
import toph.service.User
import toph.service.ZioService
import zio.Cause
import zio.IO
import zio.Task
import zio.UIO
import zio.ZIO

object UserService:

  def apply(accountService: AccountService): Task[ZioService.ZUserService[Token]] =
    ZIO.succeed(Impl(accountService))

  private class Impl(accountService: AccountService) extends ZioService.ZUserService[Token]:

    override def update(request: UpdateUser, token: Token): IO[StatusException, User] =
      for
        account <- accountService
                     .update(token.account.key, AccountService.Update(request.name))(using
                       TophExecutionContext.identifiedAccount(token.account),
                     )
                     .flatMapError(handleUpdateError(token))
        user    <- UserTransformer
                     .from(account)
                     .flatMapError(handleTransformUserError(account))
      yield user

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
