package webapi.service

import core.KeyGenerator
import io.grpc.Status
import io.grpc.StatusException
import webapi.converter.AccountConverter
import webapi.converter.GRPCAccountConverter
import webapi.grpc.{Account => GRPCAccount}
import webapi.grpc.AccountActivation
import webapi.grpc.AccountKey
import webapi.grpc.AccountReponse
import webapi.grpc.AccountUpdate
import webapi.grpc.ZioGrpc.ZAccountService
import webapi.manager.AccountManager
import webapi.model.Account
import webapi.model.UserClaims
import zio.IO
import zio.RIO
import zio.ZIO

object AccountService:

  def apply(): RIO[AccountManager & KeyGenerator, ZAccountService[UserClaims]] =
    for
      manager      <- ZIO.service[AccountManager]
      keyGenerator <- ZIO.service[KeyGenerator]
    yield Impl(manager, keyGenerator)

  private class Impl(manager: AccountManager, keyGenerator: KeyGenerator) extends ZAccountService[UserClaims]:

    override def activate(
        request: AccountActivation,
        claims: UserClaims
    ): IO[StatusException, AccountReponse] =
      for
        _ <- checkClaims(claims, request.email)
        _ <- manager
               .activate(request.email, request.code)
               .tapErrorCause(ZIO.logErrorCause(s"It was impossible to activate account ${request.email}!", _))
               .mapError(_ => Status.INTERNAL.asException)
      yield AccountReponse(email = request.email)

    override def get(request: AccountKey, claims: UserClaims): IO[StatusException, GRPCAccount] =
      for
        _        <- checkClaims(claims, request.email)
        account  <-
          manager
            .get(request.email)
            .mapError(
              Status.INTERNAL.withDescription("It was impossible to read the account data!").withCause(_).asException()
            )
            .someOrFail(Status.NOT_FOUND.asException())
            .tapErrorCause(ZIO.logWarningCause("Error ocurred while getting account data!", _))
        response <- GRPCAccountConverter
                      .from(account)
                      .tapErrorCause(ZIO.logErrorCause("An error ocurred while converting Account -> GRPCAccount!", _))
                      .mapError(Status.INTERNAL.withCause(_).asException())
      yield response

    override def create(request: GRPCAccount, claims: UserClaims): IO[StatusException, AccountReponse] =
      for
        _       <- checkClaims(claims, request.email)
        account <- AccountConverter
                     .from(request, keyGenerator)
                     .tapErrorCause(ZIO.logErrorCause("It was impossible to read the request!", _))
                     .mapError(_ => Status.INVALID_ARGUMENT.asException)
        added   <- manager
                     .add(account)
                     .tapErrorCause(ZIO.logErrorCause("It was impossible to add a new account!", _))
                     .mapError(_ => Status.INTERNAL.asException)
      yield AccountReponse(email = added.email)

    override def remove(request: AccountKey, claims: UserClaims): IO[StatusException, AccountReponse] =
      for
        _       <- checkClaims(claims, request.email)
        opt     <- manager
                     .remove(request.email)
                     .tapErrorCause(ZIO.logErrorCause("It was impossible to remove account.", _))
                     .mapError(_ => Status.INTERNAL.asException)
        account <- opt match
                     case Some(account) => ZIO.succeed(account)
                     case None          => ZIO.fail(Status.NOT_FOUND.asException)
      yield AccountReponse(email = account.email)

    override def update(request: AccountUpdate, claims: UserClaims): IO[StatusException, AccountReponse] =
      request.newName match
        case Some(newName) =>
          for
            _      <- checkClaims(claims, request.email)
            opt    <- manager
                        .update(request.email, Account.Update(newName))
                        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to update account ${request.email}!", _))
                        .mapError(_ => Status.INTERNAL.asException)
            result <- opt match
                        case Some(value) => ZIO.succeed(AccountReponse(email = value.email))
                        case None        => ZIO.fail(Status.NOT_FOUND.asException)
          yield result
        case _             => ZIO.fail(Status.INVALID_ARGUMENT.asException)

    private def checkClaims(claims: UserClaims, email: String): IO[StatusException, UserClaims] =
      claims.email match
        case Some(value) if value == email => ZIO.succeed(claims)
        case _                             => ZIO.fail(StatusException(Status.PERMISSION_DENIED))
