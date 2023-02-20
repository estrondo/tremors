package webapi.manager

import webapi.model.User
import webapi.repository.UserRepository
import zio.Task
import zio.ZIO
import webapi.model.User.Update

trait UserManager:

  def add(user: User): Task[User]

  def get(email: String): Task[Option[User]]

  def update(email: String, update: User.Update): Task[Option[User]]

  def remove(email: String): Task[Option[User]]

object UserManager:

  def apply(repository: UserRepository): UserManager =
    Impl(repository)

  private class Impl(repository: UserRepository) extends UserManager:

    override def add(user: User): Task[User] =
      repository
        .add(user)
        .tap(_ => ZIO.logDebug("An user has been added."))
        .tapErrorCause(ZIO.logErrorCause("It was impossible to add an user!", _))

    override def get(email: String): Task[Option[User]] =
      repository.get(email)

    override def update(email: String, update: Update): Task[Option[User]] =
      repository
        .update(email, update)
        .tap(_ => ZIO.logDebug(s"User $email has been updated."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to update user $email!", _))

    override def remove(email: String): Task[Option[User]] =
      repository
        .remove(email)
        .tap(_ => ZIO.logDebug(s"User $email has been removed."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to remove user $email!", _))
