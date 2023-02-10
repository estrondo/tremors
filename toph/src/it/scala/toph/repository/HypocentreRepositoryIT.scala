package toph.repository

import farango.DocumentCollection
import farango.data.ArangoConversion.convertToKey
import farango.zio.given
import testkit.zio.repository.RepositoryIT
import toph.IT
import toph.fixture.HypocentreFixture
import toph.model.Hypocentre
import zio.Scope
import zio.Task
import zio.ZIO
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.assertTrue

import HypocentreRepository.given
import HypocentreRepository.Document

object HypocentreRepositoryIT extends IT:

  given RepositoryIT[HypocentreRepository, Hypocentre] with
    override def create(collection: DocumentCollection): Task[HypocentreRepository] =
      HypocentreRepository(collection)

    override def get(collection: DocumentCollection, value: Hypocentre): Task[Option[Hypocentre]] =
      collection.get[Document](convertToKey(value.key))

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
