package toph.repository

import farango.DocumentCollection
import farango.data.Key
import farango.zio.given
import testkit.zio.repository.RepositoryIT
import toph.IT
import toph.fixture.HypocentreDataFixture
import toph.fixture.PointFixture
import toph.geom.CoordinateSequenceFactory
import toph.geom.create
import zio.RIO
import zio.Scope
import zio.Task
import zio.ZIO
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.TestResult
import zio.test.assertTrue

import HypocentreDataRepository.given
import HypocentreDataRepository.Document
import toph.model.data.HypocentreData

object HypocentreDataRepositoryIT extends IT:

  given RepositoryIT[HypocentreDataRepository, HypocentreData] with
    override def create(collection: DocumentCollection): Task[HypocentreDataRepository] =
      HypocentreDataRepository(collection)

    override def get(collection: DocumentCollection, value: HypocentreData): Task[Option[HypocentreData]] =
      collection.get[Document](Key.safe(value.key))

    override def get(repository: HypocentreDataRepository, value: HypocentreData): Task[Option[HypocentreData]] =
      ???

    override def insert(repository: HypocentreDataRepository, value: HypocentreData): Task[Any] =
      repository.add(value)

    override def remove(repository: HypocentreDataRepository, value: HypocentreData): Task[Any] =
      repository.remove(value.key)

    override def update(
        repository: HypocentreDataRepository,
        originalValue: HypocentreData,
        updateValue: Any
    ): Task[Any] =
      ???

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("A HypocentreRepository")(
      suite("With Arango's container")(
        test("It should insert a hypocentre into collection.") {
          RepositoryIT.testAdd(HypocentreDataFixture.createRandom())
        },
        test("It should remove a hypocentre from collection.") {
          RepositoryIT.testRemove(HypocentreDataFixture.createRandom())
        }
      ).provideSomeLayer(
        RepositoryIT.of[HypocentreDataRepository, HypocentreData]
      ) @@ TestAspect.sequential
    )
