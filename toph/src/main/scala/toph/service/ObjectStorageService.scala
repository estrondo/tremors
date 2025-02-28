package toph.service

import scala.util.matching.Regex
import toph.TophException
import toph.context.TophExecutionContext
import toph.model.objectstorage.Context
import toph.model.objectstorage.Error
import toph.model.objectstorage.FolderPath
import toph.model.objectstorage.ObjectPath
import zio.Cause
import zio.Exit
import zio.Task
import zio.UIO
import zio.ZIO
import zio.ZIOAspect

import scala.reflect.ClassTag

object ObjectStorageService:

  val ValidPathFragmentRegex: Regex = """^[a-zA-Z0-9\-+_]+$""".r

  def annotate[O : ClassTag](requestId: String, operationId: String) =
    ZIOAspect.annotated(
      "objectStorageService.requestId" -> requestId,
      "objectStorageService.operationId" -> operationId,
      "objectStorageService.type" -> summon[ClassTag[O]].runtimeClass.getSimpleName()
    )

  def createFolderPath(name: String)(using ctx: Context): Task[FolderPath] = ZIO.attempt {
    val nameIsValid = ValidPathFragmentRegex.matches(name)
    if nameIsValid then FolderPath(ctx.owner, name)
    else throw TophException.ObjectStorage(s"Invalid folder name: $name!")
  }

  def createObjectPath(folder: String, name: String)(using ctx: Context): Task[ObjectPath] = ZIO.attempt {
    val folderIsValid = ValidPathFragmentRegex.matches(folder)
    val nameIsValid   = ValidPathFragmentRegex.matches(name)

    if folderIsValid && nameIsValid then ObjectPath(ctx.owner, folder, name)
    else if !folderIsValid then throw TophException.ObjectStorage(s"Invalid folder: $folder!")
    else if !nameIsValid then throw TophException.ObjectStorage(s"Invalid name: $name!")
    else throw TophException.ObjectStorage(s"Invalid folder ($folder) and name ($name)!")
  }

  def reportOperationError(requestId: String, operationId: String)(cause: Cause[Throwable]): UIO[Error] =
    val (code, message) = findErrorCodeAndMessage(cause)
    ZIO.logErrorCause("An error happened during the processing of an operation.", cause) *> Exit.succeed(
      Error(
        id = operationId,
        code = code,
        message = message,
        causes = Seq.empty,
      ),
    )

  private def findErrorCodeAndMessage(cause: Cause[Throwable]): (String, String) =
    cause match
      case Cause.Fail(value, _) => ("failure", "Unable to complete the operation.")
      case Cause.Die(value, _)  => ("error", "An error happened.")
      case _                    => ("unexpected", "An unexpected error happened.")

  sealed trait GetContext extends (TophExecutionContext => Context)

  class FixedOwnerContext(name: String) extends GetContext:

    override def apply(v1: TophExecutionContext): Context = Context(
      owner = name,
      user = v1.owner.key,
    )

  class UserContext extends GetContext:
    override def apply(v1: TophExecutionContext): Context = Context(
      owner = v1.owner.key,
      user = v1.owner.key,
    )
