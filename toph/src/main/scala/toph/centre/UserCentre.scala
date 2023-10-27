package toph.centre

import toph.model.TophUser
import toph.repository.UserRepository
import zio.Task
import zio.ZIO

trait UserCentre:

  def add(user: TophUser): Task[TophUser]

  def update(id: String, update: UserCentre.Update): Task[TophUser]

object UserCentre:

  def apply(repository: UserRepository): UserCentre =
    Impl(repository)

  case class Update(name: String)

  private class Impl(repository: UserRepository) extends UserCentre:

    override def add(user: TophUser): Task[TophUser] =
      repository
        .add(user)
        .tap(_ => ZIO.logInfo(s"User ${user.id} was added."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to add user ${user.id}!", _))

    override def update(id: String, update: Update): Task[TophUser] =
      repository
        .update(id, UserRepository.Update(update.name))
        .tap(_ => ZIO.logInfo(s"User $id was updated."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to updated user $id!", _))
