package toph.service

import com.softwaremill.macwire.wire
import toph.TimeService
import toph.context.TophExecutionContext
import toph.model.objectstorage.Context
import toph.model.objectstorage.EmptyUpdateOperation
import toph.model.objectstorage.Error
import toph.model.objectstorage.Item
import toph.model.objectstorage.ObjectContent
import toph.model.objectstorage.ObjectItem
import toph.model.objectstorage.ObjectStorageResponse
import toph.model.objectstorage.ObjectStorageResult
import toph.model.objectstorage.ObjectStorageUpdateOperation
import toph.model.objectstorage.ObjectStorageUpdateRequest
import toph.model.objectstorage.RemoveFolderOperation
import toph.model.objectstorage.RemoveFolderResult
import toph.model.objectstorage.RemoveObjectOperation
import toph.model.objectstorage.RemoveObjectResult
import toph.model.objectstorage.UpdateObjectOperation
import toph.model.objectstorage.UpdateObjectResult
import toph.repository.ObjectStorageRepository
import toph.repository.objectstorage.CreateObject
import toph.repository.objectstorage.UpdateObject
import toph.repository.objectstorage.UpdateRepository
import toph.service.ObjectStorageService.GetContext
import toph.service.ObjectStorageUpdateService.PartiallyApplied
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import zio.Cause
import zio.Exit
import zio.Task
import zio.ZIO

trait ObjectStorageUpdateService:

  def apply[B]: PartiallyApplied[B] = PartiallyApplied(this)

  def execute(request: ObjectStorageUpdateRequest)(using TophExecutionContext): Task[ObjectStorageResponse]

object ObjectStorageUpdateService:

  def apply(
      repository: ObjectStorageRepository,
      keyGenerator: KeyGenerator,
      timeService: TimeService,
      getContext: GetContext,
  ): ObjectStorageUpdateService =
    wire[Impl]

  private def orEmpty(boolean: Boolean, bytes: Array[Byte]) = if boolean then bytes else Array.empty[Byte]

  class PartiallyApplied[B](service: ObjectStorageUpdateService):

    def apply[A, B](
        command: A,
    )(using ObjectStorageUpdateRequest.From[A], ObjectStorageResponse.To[B], TophExecutionContext): Task[B] =
      for
        convertedRequest  <- summon[ObjectStorageUpdateRequest.From[A]](command)
        response          <- service.execute(convertedRequest)
        convertedResponse <- summon[ObjectStorageResponse.To[B]](response)
      yield convertedResponse

  class Impl(
      repository: ObjectStorageRepository,
      keyGenerator: KeyGenerator,
      timeService: TimeService,
      getContext: GetContext,
  ) extends ObjectStorageUpdateService:

    override def execute(request: ObjectStorageUpdateRequest)(using TophExecutionContext): Task[ObjectStorageResponse] =
      given Context = getContext(summon[TophExecutionContext])
      for results <- repository.transaction(execute(_, request))
      yield ObjectStorageResponse(
        id = request.id,
        results = results,
      )

    private def execute(updateRepository: UpdateRepository, request: ObjectStorageUpdateRequest)(using
        Context,
    ): Task[Seq[ObjectStorageResult]] =
      ZIO.foreach(request.operations)(executeOperation(updateRepository, request.id))

    private def executeOperation(updateRepository: UpdateRepository, requestId: String)(
        operation: ObjectStorageUpdateOperation,
    )(using Context): Task[ObjectStorageResult] =
      operation match
        case operation: UpdateObjectOperation =>
          updateObject(updateRepository, operation)
            .catchAllCause(
              ObjectStorageService.reportOperationError(requestId, operation.id),
            ) @@ ObjectStorageService.annotate[UpdateObjectOperation](requestId, operation.id)
        case operation: RemoveObjectOperation =>
          executeRemoveObject(updateRepository, operation)
            .catchAllCause(
              ObjectStorageService.reportOperationError(requestId, operation.id),
            ) @@ ObjectStorageService.annotate[RemoveObjectOperation](requestId, operation.id)
        case operation: RemoveFolderOperation =>
          executeRemoveFolder(updateRepository, operation)
            .catchAllCause(
              ObjectStorageService.reportOperationError(requestId, operation.id),
            ) @@ ObjectStorageService.annotate[RemoveFolderOperation](requestId, operation.id)
        case EmptyUpdateOperation             =>
          Exit.succeed(
            Error(
              id = "",
              code = "empty-operation",
              message = "Empty operation",
              causes = Nil,
            ),
          )

    private def updateObject(
        updateRepository: UpdateRepository,
        operation: UpdateObjectOperation,
    )(using ctx: Context): Task[UpdateObjectResult] =
      for
        objectPath <- ObjectStorageService.createObjectPath(operation.folder, operation.name)
        result     <- updateRepository.search(objectPath)
        action     <- result match
                        case Some(item) =>
                          updateRepository.update(
                            UpdateObject(
                              key = item.key,
                              contentType = operation.contentType,
                              content = operation.content,
                              now = timeService.zonedDateTimeNow(),
                            ),
                          )
                        case None       =>
                          updateRepository.create(
                            CreateObject(
                              key = keyGenerator.generate(KeyLength.L3),
                              owner = ctx.owner,
                              folder = operation.folder,
                              name = operation.name,
                              contentType = operation.contentType,
                              content = operation.content,
                              now = timeService.zonedDateTimeNow(),
                            ),
                          )
      yield UpdateObjectResult(
        id = operation.id,
        name = operation.name,
        folder = operation.folder,
        contentType = operation.contentType,
        content = orEmpty(operation.shouldReturnContent, operation.content),
      )

    private def executeRemoveObject(
        updateRepository: UpdateRepository,
        operation: RemoveObjectOperation,
    )(using ctx: Context): Task[RemoveObjectResult] =
      for
        objectPath    <- ObjectStorageService.createObjectPath(operation.folder, operation.name)
        stored        <- if !operation.shouldReturnContent then updateRepository.search(objectPath)
                         else updateRepository.load(objectPath)
        (key, content) = stored match
                           case Some(item: ObjectItem)       => (Some(item.key), Array.empty[Byte])
                           case Some(content: ObjectContent) => (Some(content.key), content.content)
                           case _                            => (None, Array.empty[Byte])
        _             <- if key.isDefined then updateRepository.removeObject(key.get) else ZIO.unit
      yield RemoveObjectResult(
        id = operation.id,
        name = operation.name,
        folder = operation.folder,
        existed = key.isDefined,
        content = content,
      )

    private def executeRemoveFolder(
        updateRepository: UpdateRepository,
        operation: RemoveFolderOperation,
    )(using ctx: Context): Task[RemoveFolderResult] =
      for
        folderPath <- ObjectStorageService.createFolderPath(operation.name)
        content    <- if !operation.shouldReturnContent then ZIO.succeed(Seq.empty[ObjectItem])
                      else updateRepository.load(folderPath)
        _          <- updateRepository.removeFolder(folderPath)
      yield RemoveFolderResult(
        id = operation.id,
        name = operation.name,
        items =
          for item <- content
          yield Item(
            name = item.name,
            folder = item.folder,
            contentType = item.contentType,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt,
          ),
      )
