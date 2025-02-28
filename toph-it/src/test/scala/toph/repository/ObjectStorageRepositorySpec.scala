package toph.repository

import scala.util.Random
import toph.createRandomString
import toph.model.objectstorage.FolderPath
import toph.model.objectstorage.ObjectContent
import toph.model.objectstorage.ObjectPath
import toph.repository.TokenRepositorySpec.collectionManagerLayer
import toph.repository.objectstorage.CreateObjectFixture
import toph.repository.objectstorage.UpdateObject
import tremors.ZonedDateTimeFixture
import tremors.generator.KeyGenerator
import tremors.zio.farango.FarangoTestContainer
import zio.ZIO
import zio.ZLayer
import zio.test.*

object ObjectStorageRepositorySpec extends TophRepositorySpec:

  private val createNewObject =
    for
      repository   <- ZIO.service[ObjectStorageRepository]
      createObject <- repository.transaction { repo =>
                        repo.create(CreateObjectFixture.createRandom())
                      }
    yield createObject

  def spec = suite("ObjectStorageRepository")(
    suite("Update operations.")(
      test("It should create a object.") {
        val created = CreateObjectFixture.createRandom()

        // noinspection OptionEqualsSome
        for
          _      <- ZIO.serviceWithZIO[ObjectStorageRepository](_.transaction { updateRepository =>
                      updateRepository.create(created)
                    })
          result <-
            ZIO.serviceWithZIO[ObjectStorageRepository](
              _.load(ObjectPath(created.owner, created.folder, created.name)),
            )

          objectContent = result.get
        yield assertTrue(
          objectContent.content.toList == created.content.toList,
          objectContent.contentType == created.contentType,
          objectContent.name == created.name,
          objectContent.key == created.key,
          objectContent.name == created.name,
          objectContent.folder == created.folder,
        )
      },
      test("It should update an object.") {
        val createObject   = CreateObjectFixture.createRandom()
        val newContent     = createRandomString(Random(), 200).getBytes
        val updatedAt      = ZonedDateTimeFixture.createRandom()
        val newContentType = createRandomString(Random(), 10)
        val objectPath     = ObjectPath(createObject.owner, createObject.folder, createObject.name)

        // noinspection OptionEqualsSome
        for
          repository   <- ZIO.service[ObjectStorageRepository]
          _            <- repository.transaction { repo =>
                            repo.create(createObject)
                          }
          _            <- repository.transaction { repo =>
                            repo.update(
                              UpdateObject(
                                key = createObject.key,
                                contentType = newContentType,
                                content = newContent,
                                now = updatedAt,
                              ),
                            )
                          }
          result       <- repository.load(objectPath)
          objectContent = result.get
        yield assertTrue(
          objectContent.content.toList == newContent.toList,
          objectContent.contentType == newContentType,
          objectContent.name == createObject.name,
          objectContent.key == createObject.key,
          objectContent.folder == createObject.folder,
        )
      },
      test("It should load object content.") {
        for
          created      <- createNewObject
          result       <- ZIO.serviceWithZIO[ObjectStorageRepository](
                            _.transaction(x => x.load(ObjectPath(created.owner, created.folder, created.name))),
                          )
          objectContent = result.get
        yield assertTrue(
          objectContent.content.toList == created.content.toList,
          objectContent.name == created.name,
          objectContent.contentType == created.contentType,
          objectContent.key == created.key,
        )
      },
      test("It should search an object.") {
        for
          created   <- createNewObject
          result    <- ZIO.serviceWithZIO[ObjectStorageRepository](
                         _.transaction(x => x.search(ObjectPath(created.owner, created.folder, created.name))),
                       )
          objectItem = result.get
        yield assertTrue(
          objectItem.key == created.key,
          objectItem.createdAt == created.now,
          objectItem.folder == created.folder,
          objectItem.name == created.name,
          objectItem.contentType == created.contentType,
          objectItem.updatedAt == created.now,
        )
      },
      test("It should remove an object.") {
        for
          created <- createNewObject
          _       <- ZIO.serviceWithZIO[ObjectStorageRepository](_.transaction(x => x.removeObject(created.key)))
          result  <- ZIO.serviceWithZIO[ObjectStorageRepository] {
                       _.load(ObjectPath(created.owner, created.folder, created.name))
                     }
        yield assertTrue(
          result.isEmpty,
        )
      },
      test("It should remove a folder.") {
        for
          created <- createNewObject
          _       <- ZIO.serviceWithZIO[ObjectStorageRepository] {
                       _.transaction(x => x.removeFolder(FolderPath(created.owner, created.folder)))
                     }
          result  <- ZIO.serviceWithZIO[ObjectStorageRepository] {
                       _.load(ObjectPath(created.owner, created.folder, created.name))
                     }
        yield assertTrue(
          result.isEmpty,
        )
      },
    ),
    suite("Read operation")(
      test("It should load a folder.") {
        val folder   = createRandomString(Random(), 10)
        val template = CreateObjectFixture.createRandom()
        val owner    = template.owner
        val objects  = (for _ <- 1 to 10
        yield template.copy(
          folder = folder,
          key = KeyGenerator.short(),
        )).toList

        for
          repository <- ZIO.service[ObjectStorageRepository]
          _          <- repository.transaction { x =>
                          ZIO.foreach(objects)(x.create)
                        }
          items      <- repository.transaction(_.load(FolderPath(owner, folder)))
        yield assertTrue(
          items.map(_.key).toSet == objects.map(_.key).toSet,
        )
      },
      test("It should load an object.") {
        for
          repository         <- ZIO.service[ObjectStorageRepository]
          createObject       <- repository.transaction(_.create(CreateObjectFixture.createRandom()))
          result             <- repository.load(ObjectPath(createObject.owner, createObject.folder, createObject.name))
          Some(objectContent) = result
        yield assertTrue(
          objectContent.content.toList == createObject.content.toList,
          objectContent.contentType == createObject.contentType,
          objectContent.name == createObject.name,
          objectContent.folder == createObject.folder,
          objectContent.key == createObject.key,
        )
      },
    ),
  ).provideSome(
    FarangoTestContainer.arangoContainer,
    FarangoTestContainer.farangoDB,
    FarangoTestContainer.farangoDatabase(),
    FarangoTestContainer.farangoCollection(),
    collectionManagerLayer,
    ZLayer.fromFunction(ObjectStorageRepository.apply),
  ) @@ TestAspect.sequential
