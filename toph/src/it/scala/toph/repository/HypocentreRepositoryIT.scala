package toph.repository

import toph.IT
import zio.Scope
import zio.test.Spec
import zio.test.TestEnvironment
import testkit.zio.repository.RepositoryIT
import toph.fixture.HypocentreFixture
import zio.ZIO
import zio.test.assertTrue
import HypocentreRepository.given
import farango.zio.given
import zio.test.TestAspect
import toph.model.Hypocentre
import farango.DocumentCollection
import zio.Task
import HypocentreRepository.Document

object HypocentreRepositoryIT extends IT:

  given RepositoryIT[HypocentreRepository, Hypocentre] with
    override def create(collection: DocumentCollection): Task[HypocentreRepository] =
      HypocentreRepository(collection)

    override def get(collection: DocumentCollection, key: String): Task[Option[Hypocentre]] =
      collection.get[Document](key)

    override def getKey(value: Hypocentre): String = value.key

    override def insert(repository: HypocentreRepository, value: Hypocentre): Task[Any] =
      repository.add(value)

    override def remove(repository: HypocentreRepository, value: Hypocentre): Task[Any] =
      repository.remove(value.key)

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("A HypocentreRepository")(
      suite("With Arango's container")(
        test("It should insert a hypocentre into collection.") {
          RepositoryIT.testAdd(HypocentreFixture.createRandom())
        },
        test("It should remove a hypocentre from collection.") {
          RepositoryIT.testRemove(HypocentreFixture.createRandom())
        }
      ).provideSomeLayer(
        RepositoryIT.of[HypocentreRepository, Hypocentre]
      ) @@ TestAspect.sequential
    )
