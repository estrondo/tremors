package toph.repository

import MagnitudeDataRepository.Document
import farango.DocumentCollection
import farango.data.Key
import farango.zio.given
import testkit.zio.repository.RepositoryIT
import toph.Spec
import toph.fixture.MagnitudeDataFixture
import toph.model.data.MagnitudeData
import zio.Scope
import zio.Task
import zio.ZIO
import zio.test.TestEnvironment

object MagnitudeDataRepositoryIT extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A MagnitudeRepository")(
      test("It should add a magnitude into collection.") {
        RepositoryIT.testAdd(MagnitudeDataFixture.createRandom())
      },
      test("It should remove a magnitude from collection.") {
        RepositoryIT.testRemove(MagnitudeDataFixture.createRandom())
      }
    ).provideSome(
      RepositoryIT.of[MagnitudeDataRepository, MagnitudeData]
    )

  private given RepositoryIT[MagnitudeDataRepository, MagnitudeData] with

    override def create(collection: DocumentCollection): Task[MagnitudeDataRepository] =
      ZIO.succeed(MagnitudeDataRepository(collection))

    override def get(collection: DocumentCollection, value: MagnitudeData): Task[Option[MagnitudeData]] =
      collection.get[Document](Key.safe(value.key))

    override def get(repository: MagnitudeDataRepository, value: MagnitudeData): Task[Option[MagnitudeData]] =
      repository.get(value.key)

    override def insert(repository: MagnitudeDataRepository, value: MagnitudeData): Task[Any] =
      repository.add(value)

    override def remove(repository: MagnitudeDataRepository, value: MagnitudeData): Task[Any] =
      repository.remove(value.key)

    override def update(
        repository: MagnitudeDataRepository,
        originalValue: MagnitudeData,
        updateValue: Any
    ): Task[Any] =
      ???
