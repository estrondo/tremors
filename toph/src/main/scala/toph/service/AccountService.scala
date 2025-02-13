package toph.service

import toph.context.TophExecutionContext
import toph.model.Account
import toph.model.ProtoAccount
import toph.repository.AccountRepository
import tremors.generator.KeyGenerator
import zio.Task
import zio.ZIO
import zio.ZIOAspect

trait AccountService:

  def add(account: Account)(using TophExecutionContext): Task[Account]

  def update(key: String, update: AccountService.Update)(using TophExecutionContext): Task[Account]

  def findOrCreate(email: String, protoAccount: ProtoAccount)(using TophExecutionContext): Task[Account]

object AccountService:

  def apply(repository: AccountRepository, keyGenerator: KeyGenerator): AccountService =
    Impl(repository, keyGenerator)

  case class Update(name: String)

  private class Impl(repository: AccountRepository, keyGenerator: KeyGenerator) extends AccountService:

    override def add(account: Account)(using TophExecutionContext): Task[Account] =
      repository
        .add(account)
        .tap(_ => ZIO.logInfo(s"Account was added."))
        .tapErrorCause(
          ZIO.logWarningCause(s"Unable to add the account.", _),
        ) @@ withKey(account.key)

    override def update(key: String, update: Update)(using TophExecutionContext): Task[Account] =
      repository
        .update(key, AccountRepository.Update(update.name))
        .tap(_ => ZIO.logInfo(s"Account was updated."))
        .tapErrorCause(
          ZIO.logErrorCause(s"Unable to update the account", _),
        ) @@ withKey(key)

    override def findOrCreate(email: String, protoAccount: ProtoAccount)(using TophExecutionContext): Task[Account] =
      repository.searchByEmail(email).flatMap { result =>
        if result.isDefined then ZIO.succeed(result.get)
        else add(defaultNewAccount(email, protoAccount))
      } @@ withEmail(email)

    private def withKey(value: String)(using TophExecutionContext) = annotate("key", value)

    private def withEmail(value: String)(using TophExecutionContext) = annotate("email", value)

    private def annotate(prop: String, value: String)(using TophExecutionContext) = ZIOAspect.annotated(
      s"accountService.$prop" -> value,
      "accountService.owner"  -> TophExecutionContext().owner.toString,
    )

    private def defaultNewAccount(email: String, protoAccount: ProtoAccount): Account =
      Account(
        key = keyGenerator.short(),
        email = email,
        name = protoAccount.name.getOrElse(""),
      )
