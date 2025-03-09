package toph.grpc.impl

import io.grpc.Status
import io.grpc.StatusException
import toph.context.TophExecutionContext
import toph.security.AccessToken
import toph.service.AccountService
import toph.v1.grpc.GrpcAccount
import toph.v1.grpc.GrpcUpdateAccount
import toph.v1.grpc.ZioGrpc
import zio.Cause
import zio.IO
import zio.Task
import zio.ZIO

object GrpcAccountService:

  def apply(accountService: AccountService): Task[ZioGrpc.ZAccountService[AccessToken]] =
    ZIO.succeed(Impl(accountService))

  class Impl(accountService: AccountService) extends ZioGrpc.ZAccountService[AccessToken]:

    override def update(request: GrpcUpdateAccount, token: AccessToken): IO[StatusException, GrpcAccount] =
      for
        updatedAccount <- accountService
                            .update(token.account.key, AccountService.Update(request.name))(using
                              TophExecutionContext.account(token.account),
                            )
                            .tapErrorCause(reportError(s"Unable to update the account=${token.account.key}!"))
                            .mapError(convertToStatusError)
        transformed    <- GrpcAccountTransformer
                            .from(updatedAccount)
                            .tapErrorCause(
                              reportError(
                                s"Unable to convert to Grpc message the account=${updatedAccount.key}!",
                              ),
                            )
                            .mapError(convertToStatusError)
      yield transformed

    private def reportError(message: => String)(cause: Cause[Throwable]) =
      ZIO.logErrorCause(message, cause)

    private def convertToStatusError(cause: Throwable) =
      StatusException(Status.INTERNAL)
