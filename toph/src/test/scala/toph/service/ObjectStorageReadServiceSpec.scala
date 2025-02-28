package toph.service

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import toph.TophSpec
import toph.model.objectstorage.FetchFolderOperationFixture
import toph.model.objectstorage.FetchFolderResult
import toph.model.objectstorage.FetchObjectOperation
import toph.model.objectstorage.FetchObjectOperationFixture
import toph.model.objectstorage.FetchObjectResult
import toph.model.objectstorage.FolderPath
import toph.model.objectstorage.Item
import toph.model.objectstorage.ObjectContentFixture
import toph.model.objectstorage.ObjectItemFixture
import toph.model.objectstorage.ObjectPath
import toph.model.objectstorage.ObjectStorageReadRequest
import toph.repository.ObjectStorageRepository
import tremors.generator.KeyGenerator
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object ObjectStorageReadServiceSpec extends ObjectStorageServiceSpec:

  def spec = suite("ObjectStorageReadService")(
    test("It should load an object by path.") {
      for
        ctx          <- nextContext
        operation     = FetchObjectOperationFixture.createRandom()
        objectContent = ObjectContentFixture
                          .createRandom()
                          .copy(
                            folder = operation.folder,
                            name = operation.name,
                          )
        request       = ObjectStorageReadRequest(
                          id = KeyGenerator.short(),
                          operations = Seq(operation),
                        )

        _                             <- repositoryLoadObject(
                                           ObjectPath(ctx.owner, operation.folder, operation.name),
                                           ZIO.succeed(Some(objectContent)),
                                         )
        response                      <- ZIO.serviceWithZIO[ObjectStorageReadService](_.execute(request))
        Seq(result: FetchObjectResult) = response.results
      yield assertTrue(
        result.id == operation.id,
        result.exists,
        result.name == operation.name,
        result.folder == operation.folder,
        result.contentType == objectContent.contentType,
        result.content.toList == objectContent.content.toList,
      )
    },
    test("It should load a folder.") {
      for
        ctx                           <- nextContext
        operation                      = FetchFolderOperationFixture.createRandom()
        items                          = (for _ <- 1 to 10
                                         yield ObjectItemFixture
                                           .createRandom()
                                           .copy(
                                             folder = operation.name,
                                           )).toList
        request                        = ObjectStorageReadRequest(
                                           id = KeyGenerator.short(),
                                           operations = Seq(operation),
                                         )
        _                             <- repositoryLoadFolder(FolderPath(ctx.owner, operation.name), ZIO.succeed(items))
        response                      <- ZIO.serviceWithZIO[ObjectStorageReadService](_.execute(request))
        Seq(result: FetchFolderResult) = response.results
      yield assertTrue(
        result.id == operation.id,
        result.name == operation.name,
        result.items.map(_.name).toSet == items.map(_.name).toSet,
      )
    },
  ).provideSome(
    SweetMockitoLayer.newMockLayer[ObjectStorageRepository],
    SweetMockitoLayer.newMockLayer[ObjectStorageService.GetContext],
    ZLayer.fromFunction(ObjectStorageReadService.apply),
  )
