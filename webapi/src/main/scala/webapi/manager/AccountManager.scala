package webapi.manager

import webapi.WebAPIException
import webapi.model.Account
import webapi.model.Account.Update
import webapi.repository.AccountRepository
import zio.Task
import zio.ZIO

trait AccountManager:

  def activate(email: String, code: String): Task[Option[Account]]

  def add(account: Account): Task[Account]

  def get(email: String): Task[Option[Account]]

  def update(email: String, update: Account.Update): Task[Option[Account]]

  def remove(email: String): Task[Option[Account]]

object AccountManager:

  def apply(repository: AccountRepository): AccountManager =
    Impl(repository)

  private class Impl(repository: AccountRepository) extends AccountManager:

    override def activate(email: String, code: String): Task[Option[Account]] =
      for
        account <- repository.get(email).someOrFail(WebAPIException.NotFound(s"Account: $email"))
        result  <- activate(account, code)
      yield result

    override def add(account: Account): Task[Account] =
      repository
        .add(account)
        .tap(_ => ZIO.logDebug("An account has been added."))
        .tapErrorCause(ZIO.logErrorCause("It was impossible to add an account!", _))

    override def get(email: String): Task[Option[Account]] =
      repository.get(email)

    override def update(email: String, update: Update): Task[Option[Account]] =
      repository
        .update(email, update)
        .tap(_ => ZIO.logDebug(s"User $email has been updated."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to update account $email!", _))

    override def remove(email: String): Task[Option[Account]] =
      repository
        .remove(email)
        .tap(_ => ZIO.logDebug(s"User $email has been removed."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to remove account $email!", _))

    private def activate(account: Account, code: String): Task[Option[Account]] =
      (account.active, account.secret) match
        case (false, secret) if secret == code =>
          repository.activate(account.email) <* ZIO.logDebug(s"Account ${account.email} has just been activated.")
        case (true, _)                         =>
          ZIO.logDebug(s"Account ${account.name} is already activated.") as Some(account)
        case _                                 =>
          ZIO.logDebug("Secret is wrong.") as None
