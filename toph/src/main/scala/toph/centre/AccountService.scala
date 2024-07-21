package toph.centre

import toph.context.TophExecutionContext
import toph.model.Account
import toph.repository.AccountRepository
import zio.Task
import zio.ZIO
import zio.ZIOAspect

trait AccountService:

  def add(account: Account)(using TophExecutionContext): Task[Account]

  def update(key: String, update: AccountService.Update)(using TophExecutionContext): Task[Account]

  def findOrCreate(email: String): Task[Account]

object AccountService:

  def apply(repository: AccountRepository): AccountService =
    Impl(repository)

  case class Update(name: String)

  private class Impl(repository: AccountRepository) extends AccountService:

    override def add(account: Account)(using TophExecutionContext): Task[Account] =
      repository
        .add(account)
        .tap(_ => ZIO.logInfo(s"Account was added."))
        .tapErrorCause(
          ZIO.logWarningCause(s"Unable to add the account.", _),
        ) @@ annotate(account.key)

    override def update(key: String, update: Update)(using TophExecutionContext): Task[Account] =
      repository
        .update(key, AccountRepository.Update(update.name))
        .tap(_ => ZIO.logInfo(s"Account was updated."))
        .tapErrorCause(
          ZIO.logErrorCause(s"Unable to update the account", _),
        ) @@ annotate(key)

    override def findOrCreate(email: String): Task[Account] = ???

    private def annotate(key: String)(using TophExecutionContext) = ZIOAspect.annotated(
      "accountService.key"   -> key,
      "accountService.owner" -> TophExecutionContext().owner.toString,
    )
