package toph.centre

import toph.context.TophExecutionContext
import toph.model.TophUser
import toph.repository.UserRepository
import zio.Task
import zio.ZIO
import zio.ZIOAspect

trait UserCentre:

  def add(user: TophUser)(using TophExecutionContext): Task[TophUser]

  def update(id: String, update: UserCentre.Update)(using TophExecutionContext): Task[TophUser]

object UserCentre:

  def apply(repository: UserRepository): UserCentre =
    Impl(repository)

  case class Update(name: String)

  private class Impl(repository: UserRepository) extends UserCentre:

    override def add(user: TophUser)(using TophExecutionContext): Task[TophUser] =
      repository
        .add(user)
        .tap(_ => ZIO.logInfo(s"User was added."))
        .tapErrorCause(
          ZIO.logWarningCause(s"It was impossible to add user.", _)
        ) @@ annotate(user.id)

    override def update(id: String, update: Update)(using TophExecutionContext): Task[TophUser] =
      repository
        .update(id, UserRepository.Update(update.name))
        .tap(_ => ZIO.logInfo(s"User was updated."))
        .tapErrorCause(
          ZIO.logErrorCause(s"It was impossible to updated user!", _)
        ) @@ annotate(id)

    private def annotate(id: String)(using TophExecutionContext) = ZIOAspect.annotated(
      "userCentre.userId"         -> id,
      "userCentre.executionOwner" -> TophExecutionContext().owner.toString()
    )
