package webapi.service

import core.KeyGenerator
import grpc.webapi.account.AccountActivation
import grpc.webapi.account.AccountKey
import grpc.webapi.account.AccountReponse
import grpc.webapi.account.AccountUpdate
import grpc.webapi.account.ZioAccount.ZAccountService
import grpc.webapi.account.{Account => GRPCAccount}
import io.grpc.Status
import scalapb.zio_grpc.RequestContext
import webapi.converter.AccountConverter
import webapi.manager.AccountManager
import webapi.model.Account
import zio.IO
import zio.RIO
import zio.ZIO

object AccountService:

  def apply(): RIO[AccountManager & KeyGenerator, ZAccountService[RequestContext]] =
    for
      manager      <- ZIO.service[AccountManager]
      keyGenerator <- ZIO.service[KeyGenerator]
    yield Impl(manager, keyGenerator)

  private class Impl(manager: AccountManager, keyGenerator: KeyGenerator) extends ZAccountService[RequestContext]:

    override def activate(request: AccountActivation, context: RequestContext): IO[Status, AccountReponse] =
      for _ <- manager
                 .activate(request.email, request.code)
                 .tapErrorCause(ZIO.logErrorCause(s"It was impossible to activate account ${request.email}!", _))
                 .mapError(_ => Status.INTERNAL)
      yield AccountReponse(email = request.email)

    override def create(request: GRPCAccount, context: RequestContext): IO[Status, AccountReponse] =
      for
        account <- AccountConverter
                     .from(request, keyGenerator)
                     .tapErrorCause(ZIO.logErrorCause("It was impossible to read the request!", _))
                     .mapError(_ => Status.INVALID_ARGUMENT)
        added   <- manager
                     .add(account)
                     .tapErrorCause(ZIO.logErrorCause("It was impossible to add a new account!", _))
                     .mapError(_ => Status.INTERNAL)
      yield AccountReponse(email = added.email)

    override def remove(request: AccountKey, context: RequestContext): IO[Status, AccountReponse] =
      for
        opt     <- manager
                     .remove(request.email)
                     .tapErrorCause(ZIO.logErrorCause("It was impossible to remove account.", _))
                     .mapError(_ => Status.INTERNAL)
        account <- opt match
                     case Some(account) => ZIO.succeed(account)
                     case None          => ZIO.fail(Status.NOT_FOUND)
      yield AccountReponse(email = account.email)

    override def update(request: AccountUpdate, context: RequestContext): IO[Status, AccountReponse] =
      request.newName match
        case Some(newName) =>
          for
            opt    <- manager
                        .update(request.email, Account.Update(newName))
                        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to update account ${request.email}!", _))
                        .mapError(_ => Status.INTERNAL)
            result <- opt match
                        case Some(value) => ZIO.succeed(AccountReponse(email = value.email))
                        case None        => ZIO.fail(Status.NOT_FOUND)
          yield result
        case _             => ZIO.fail(Status.INVALID_ARGUMENT)
