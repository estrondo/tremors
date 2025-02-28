package toph.service

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import toph.TimeService
import toph.TophSpec
import toph.model.objectstorage.FolderPath
import toph.model.objectstorage.ObjectContent
import toph.model.objectstorage.ObjectContentFixture
import toph.model.objectstorage.ObjectItem
import toph.model.objectstorage.ObjectItemFixture
import toph.model.objectstorage.ObjectPath
import toph.model.objectstorage.ObjectStorageUpdateRequest
import toph.model.objectstorage.RemoveFolderOperationFixture
import toph.model.objectstorage.RemoveFolderResult
import toph.model.objectstorage.RemoveObjectOperationFixture
import toph.model.objectstorage.RemoveObjectResult
import toph.model.objectstorage.UpdateObjectOperationFixture
import toph.model.objectstorage.UpdateObjectResult
import toph.repository.ObjectStorageRepository
import toph.repository.objectstorage.CreateObject
import toph.repository.objectstorage.UpdateObjectFixture
import toph.repository.objectstorage.UpdateRepository
import toph.service.ObjectStorageService.GetContext
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.test.*

object ObjectStorageUpdateServiceSpec extends ObjectStorageServiceSpec:

  private val prepareTransaction =
    for
      repository       <- ZIO.service[ObjectStorageRepository]
      updateRepository <- ZIO.service[UpdateRepository]
    yield Mockito.when(repository.transaction(ArgumentMatchers.any())).thenAnswer { i =>
      val f = i.getArgument(0).asInstanceOf[UpdateRepository => Task[?]]
      f(updateRepository)
    }

  def spec = suite("ObjectStorageUpdateService")(
    test("It should create a new object.") {
      val operation = UpdateObjectOperationFixture.createRandom()
      for
        now         <- nextTimeService
        key         <- nextKeyGenerator
        context     <- nextContext
        createObject = CreateObject(
                         key = key,
                         owner = context.owner,
                         folder = operation.folder,
                         name = operation.name,
                         contentType = operation.contentType,
                         content = operation.content,
                         now = now,
                       )
        request      = ObjectStorageUpdateRequest(
                         id = KeyGenerator.short(),
                         operations = Seq(operation),
                       )
        _           <- prepareTransaction
        _           <- updateRepositorySearch(ObjectPath(context.owner, operation.folder, operation.name), ZIO.none)
        _           <- updateRepositoryCreate(createObject, ZIO.succeed(createObject))
        response    <- ZIO.serviceWithZIO[ObjectStorageUpdateService](_.execute(request))
        result       = response.results.head
      yield assertTrue(
        result.asInstanceOf[UpdateObjectResult].id == operation.id,
      )
    },
    test("It should update an object.") {
      for
        now         <- nextTimeService
        ctx         <- nextContext
        key         <- nextKeyGenerator
        operation    = UpdateObjectOperationFixture
                         .createRandom()
        update       = UpdateObjectFixture
                         .createRandom()
                         .copy(
                           now = now,
                         )
        objectItem   = ObjectItemFixture
                         .createRandom()
                         .copy(
                           folder = operation.folder,
                           name = operation.name,
                         )
        updateObject = UpdateObjectFixture
                         .createRandom()
                         .copy(
                           key = objectItem.key,
                           now = now,
                           contentType = operation.contentType,
                           content = operation.content,
                         )
        _           <-
          updateRepositorySearch(
            ObjectPath(ctx.owner, operation.folder, operation.name),
            ZIO.succeed(Some(objectItem)),
          )
        _           <- ZIO.serviceWith[UpdateRepository] { x =>
                         Mockito
                           .when(x.update(updateObject))
                           .thenReturn(ZIO.succeed(updateObject))
                       }
        request      = ObjectStorageUpdateRequest(
                         id = KeyGenerator.generate(KeyLength.L3),
                         operations = Seq(operation),
                       )
        _           <- prepareTransaction
        response    <- ZIO.serviceWithZIO[ObjectStorageUpdateService](_.execute(request))
        result       = response.results.head
      yield assertTrue(
        result.asInstanceOf[UpdateObjectResult].id == operation.id,
      )
    },
    test("It should remove an object and return its content.") {
      for
        key          <- nextKeyGenerator
        ctx          <- nextContext
        now          <- nextTimeService
        operation     = RemoveObjectOperationFixture
                          .createRandom()
                          .copy(shouldReturnContent = true)
        request       = ObjectStorageUpdateRequest(
                          id = KeyGenerator.short(),
                          operations = Seq(operation),
                        )
        objectContent = ObjectContentFixture
                          .createRandom()
                          .copy(
                            name = operation.name,
                            folder = operation.folder,
                          )
        _            <- updateRepositoryLoadObject(
                          ObjectPath(ctx.owner, operation.folder, operation.name),
                          ZIO.succeed(Some(objectContent)),
                        )
        _            <- prepareTransaction
        _            <- SweetMockitoLayer[UpdateRepository]
                          .whenF2(_.removeObject(objectContent.key))
                          .thenReturn(())
        response     <- ZIO.serviceWithZIO[ObjectStorageUpdateService](_.execute(request))
        result        = response.results.head.asInstanceOf[RemoveObjectResult]
      yield assertTrue(
        result.id == operation.id,
        result.existed,
        result.content.toList == objectContent.content.toList,
      )
    },
    test("It should remove an object and not return its content.") {
      for
        key       <- nextKeyGenerator
        ctx       <- nextContext
        now       <- nextTimeService
        operation  = RemoveObjectOperationFixture
                       .createRandom()
                       .copy(shouldReturnContent = false)
        request    = ObjectStorageUpdateRequest(
                       id = KeyGenerator.short(),
                       operations = Seq(operation),
                     )
        objectItem = ObjectItemFixture
                       .createRandom()
                       .copy(
                         folder = operation.folder,
                         name = operation.name,
                       )
        _         <- updateRepositorySearch(
                       ObjectPath(ctx.owner, operation.folder, operation.name),
                       ZIO.succeed(Some(objectItem)),
                     )
        _         <- prepareTransaction
        _         <- SweetMockitoLayer[UpdateRepository]
                       .whenF2(_.removeObject(objectItem.key))
                       .thenReturn(())
        response  <- ZIO.serviceWithZIO[ObjectStorageUpdateService](_.execute(request))
        result     = response.results.head.asInstanceOf[RemoveObjectResult]
      yield assertTrue(
        result.id == operation.id,
        result.existed,
        result.content.isEmpty,
      )
    },
    test("It should remove an object and not return its content.") {
      for
        key       <- nextKeyGenerator
        ctx       <- nextContext
        now       <- nextTimeService
        operation  = RemoveObjectOperationFixture
                       .createRandom()
                       .copy(shouldReturnContent = false)
        request    = ObjectStorageUpdateRequest(
                       id = KeyGenerator.short(),
                       operations = Seq(operation),
                     )
        objectItem = ObjectItemFixture
                       .createRandom()
                       .copy(
                         folder = operation.folder,
                         name = operation.name,
                       )
        _         <- updateRepositorySearch(
                       ObjectPath(ctx.owner, operation.folder, operation.name),
                       ZIO.succeed(Some(objectItem)),
                     )
        _         <- prepareTransaction
        _         <- SweetMockitoLayer[UpdateRepository]
                       .whenF2(_.removeObject(objectItem.key))
                       .thenReturn(())
        response  <- ZIO.serviceWithZIO[ObjectStorageUpdateService](_.execute(request))
        result     = response.results.head.asInstanceOf[RemoveObjectResult]
      yield assertTrue(
        result.id == operation.id,
        result.existed,
        result.content.isEmpty,
      )
    },
    test("When it is trying to remove an object which doesn't exist, it should inform it.") {
      for
        key      <- nextKeyGenerator
        ctx      <- nextContext
        now      <- nextTimeService
        operation = RemoveObjectOperationFixture
                      .createRandom()
                      .copy(shouldReturnContent = false)
        request   = ObjectStorageUpdateRequest(
                      id = KeyGenerator.short(),
                      operations = Seq(operation),
                    )
        _        <- updateRepositorySearch(
                      ObjectPath(ctx.owner, operation.folder, operation.name),
                      ZIO.none,
                    )
        _        <- prepareTransaction
        response <- ZIO.serviceWithZIO[ObjectStorageUpdateService](_.execute(request))
        result    = response.results.head.asInstanceOf[RemoveObjectResult]
      yield assertTrue(
        result.id == operation.id,
        !result.existed,
        result.content.isEmpty,
        result.folder == operation.folder,
      )
    },
    test("It should remove a folder and return its content.") {
      for
        now                            <- nextTimeService
        ctx                            <- nextContext
        operation                       = RemoveFolderOperationFixture
                                            .createRandom()
                                            .copy(shouldReturnContent = true)
        objectItem                      = ObjectItemFixture
                                            .createRandom()
                                            .copy(
                                              folder = operation.name,
                                            )
        folderPath                      = FolderPath(ctx.owner, objectItem.folder)
        _                              <- updateRepositoryLoadFolder(folderPath, ZIO.succeed(List(objectItem)))
        _                              <- prepareTransaction
        request                         = ObjectStorageUpdateRequest(
                                            id = KeyGenerator.short(),
                                            operations = Seq(operation),
                                          )
        _                              <- SweetMockitoLayer[UpdateRepository].whenF2(_.removeFolder(folderPath)).thenReturn(())
        response                       <- ZIO.serviceWithZIO[ObjectStorageUpdateService](_.execute(request))
        Seq(result: RemoveFolderResult) = response.results
        Seq(item)                       = result.items
      yield assertTrue(
        result.id == operation.id,
        item.name == objectItem.name,
        item.folder == objectItem.folder,
        item.updatedAt == objectItem.updatedAt,
        item.contentType == objectItem.contentType,
        item.createdAt == objectItem.createdAt,
      )
    },
  ).provideSome(
    SweetMockitoLayer.newMockLayer[KeyGenerator],
    SweetMockitoLayer.newMockLayer[TimeService],
    SweetMockitoLayer.newMockLayer[ObjectStorageService.GetContext],
    SweetMockitoLayer.newMockLayer[ObjectStorageRepository],
    SweetMockitoLayer.newMockLayer[UpdateRepository],
    ZLayer.fromFunction(ObjectStorageUpdateService.apply),
  )

  private def updateRepositorySearch(path: ObjectPath, result: Task[Option[ObjectItem]]) =
    ZIO.serviceWithZIO[UpdateRepository] { x =>
      Mockito.when(x.search(path)).thenReturn(result)
      result
    }

  private def updateRepositoryCreate(createObject: CreateObject, result: Task[CreateObject]) =
    ZIO.serviceWithZIO[UpdateRepository] { x =>
      Mockito.when(x.create(createObject)).thenReturn(result)
      result
    }

  private def updateRepositoryLoadObject(path: ObjectPath, result: Task[Option[ObjectContent]]) =
    ZIO.serviceWithZIO[UpdateRepository] { x =>
      Mockito.when(x.load(path)).thenReturn(result)
      result
    }
  private def updateRepositoryLoadFolder(path: FolderPath, result: Task[Seq[ObjectItem]])       =
    ZIO.serviceWithZIO[UpdateRepository] { x =>
      Mockito.when(x.load(path)).thenReturn(result)
      result
    }
