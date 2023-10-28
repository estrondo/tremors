package toph.service

import io.grpc.Status
import io.grpc.StatusException
import toph.centre.UserCentre
import toph.context.TophExecutionContext
import toph.model.AuthenticatedUser
import toph.model.TophUser
import zio.Cause
import zio.IO
import zio.Task
import zio.UIO
import zio.ZIO

object UserService:

  def apply(userCentre: UserCentre): Task[ZioService.ZUserService[AuthenticatedUser]] =
    ZIO.succeed(Impl(userCentre))

  private class Impl(userCentre: UserCentre) extends ZioService.ZUserService[AuthenticatedUser]:

    override def update(request: UpdateUser, context: AuthenticatedUser): IO[StatusException, User] =
      for
        storedUser <- userCentre
                        .update(context.claims.id, UserCentre.Update(request.name))(using
                          TophExecutionContext.identifiedUser(context)
                        )
                        .flatMapError(handleUpdateError(context))
        user       <- UserTransformer
                        .from(storedUser)
                        .flatMapError(handleTransformUserError(storedUser))
      yield user

    private def handleUpdateError(context: AuthenticatedUser)(cause: Throwable): UIO[StatusException] =
      ZIO.logErrorCause(
        s"An error happened during updating of user=${context.claims.id}!",
        Cause.die(cause)
      ) as StatusException(Status.INTERNAL)

    private def handleTransformUserError(user: TophUser)(cause: Throwable): UIO[StatusException] =
      ZIO.logErrorCause(
        s"An error happened during transformation of TophUser ${user.id}.",
        Cause.die(cause)
      ) as StatusException(Status.INTERNAL)
