package toph.service

import com.softwaremill.macwire.wire
import io.github.arainko.ducktape.Transformer
import toph.context.TophExecutionContext
import toph.model.objectstorage.Context
import toph.model.objectstorage.EmptyReadOperation
import toph.model.objectstorage.Error
import toph.model.objectstorage.FetchFolderOperation
import toph.model.objectstorage.FetchFolderResult
import toph.model.objectstorage.FetchObjectOperation
import toph.model.objectstorage.FetchObjectResult
import toph.model.objectstorage.Item
import toph.model.objectstorage.ObjectStorageReadOperation
import toph.model.objectstorage.ObjectStorageReadRequest
import toph.model.objectstorage.ObjectStorageResponse
import toph.model.objectstorage.ObjectStorageResult
import toph.repository.ObjectStorageRepository
import toph.service.ObjectStorageReadService.PartiallyApplied
import toph.service.ObjectStorageService.GetContext
import zio.Exit
import zio.Task
import zio.ZIO

trait ObjectStorageReadService:

  def apply[B]: PartiallyApplied[B] = PartiallyApplied(this)

  def execute(request: ObjectStorageReadRequest)(using TophExecutionContext): Task[ObjectStorageResponse]

object ObjectStorageReadService:

  def apply(repository: ObjectStorageRepository, getContext: GetContext): ObjectStorageReadService =
    wire[Impl]

  class PartiallyApplied[B](service: ObjectStorageReadService):
    def apply[A](command: A)(using
        ObjectStorageReadRequest.From[A],
        ObjectStorageResponse.To[B],
        TophExecutionContext,
    ): Task[B] =
      for
        convertedRequest  <- summon[ObjectStorageReadRequest.From[A]](command)
        response          <- service.execute(convertedRequest)
        convertedResponse <- summon[ObjectStorageResponse.To[B]](response)
      yield convertedResponse

  class Impl(repository: ObjectStorageRepository, getContext: GetContext) extends ObjectStorageReadService:

    override def execute(request: ObjectStorageReadRequest)(using TophExecutionContext): Task[ObjectStorageResponse] =
      given Context = getContext(summon[TophExecutionContext])
      for results <- ZIO.foreach(request.operations)(execute(_, request.id))
      yield ObjectStorageResponse(
        id = request.id,
        results = results,
      )

    private def execute(operation: ObjectStorageReadOperation, requestId: String)(using
        Context,
    ): Task[ObjectStorageResult] =
      operation match
        case operation: FetchObjectOperation =>
          executeFetchObject(operation, requestId)
            .catchAllCause(
              ObjectStorageService.reportOperationError(requestId, operation.id),
            ) @@ ObjectStorageService.annotate[FetchObjectOperation](requestId, operation.id)
        case operation: FetchFolderOperation =>
          executeFetchFolder(operation, requestId)
            .catchAllCause(
              ObjectStorageService.reportOperationError(requestId, operation.id),
            ) @@ ObjectStorageService.annotate[FetchFolderOperation](requestId, operation.id)
        case EmptyReadOperation              =>
          Exit.succeed(
            Error(
              id = "",
              code = "empty-read-operation",
              message = "Empty read operation!",
              causes = Nil,
            ),
          )

    private def executeFetchObject(operation: FetchObjectOperation, requestId: String)(using Context) =
      for
        path          <- ObjectStorageService.createObjectPath(operation.folder, operation.name)
        objectContent <- repository.load(path)
      yield
        val (contentType, content) = objectContent match
          case Some(x) => (x.contentType, x.content)
          case None    => ("", Array.empty[Byte])

        FetchObjectResult(
          id = operation.id,
          name = operation.name,
          folder = operation.folder,
          contentType = contentType,
          content = content,
          exists = objectContent.isDefined,
        )

    private def executeFetchFolder(operation: FetchFolderOperation, requestId: String)(using Context) =
      for
        path  <- ObjectStorageService.createFolderPath(operation.name)
        items <- repository.load(path)
      yield FetchFolderResult(
        id = operation.id,
        name = operation.name,
        items =
          for item <- items
          yield Item(
            name = item.name,
            folder = item.folder,
            contentType = item.contentType,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt,
          ),
      )
