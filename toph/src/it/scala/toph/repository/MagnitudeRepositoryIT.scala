package toph.repository

import toph.Spec
import zio.Scope
import zio.test.TestEnvironment
import testkit.zio.repository.RepositoryIT
import toph.model.Magnitude
import farango.DocumentCollection
import zio.Task
import zio.ZIO
import MagnitudeRepository.Document
import farango.zio.given
import toph.fixture.MagnitudeFixture
import farango.data.Key

object MagnitudeRepositoryIT extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A MagnitudeRepository")(
      test("It should add a magnitude into collection.") {
        RepositoryIT.testAdd(MagnitudeFixture.createRandom())
      },
      test("It should remove a magnitude from collection.") {
        RepositoryIT.testRemove(MagnitudeFixture.createRandom())
      }
    ).provideSome(
      RepositoryIT.of[MagnitudeRepository, Magnitude]
    )

  private given RepositoryIT[MagnitudeRepository, Magnitude] with

    override def create(collection: DocumentCollection): Task[MagnitudeRepository] =
      ZIO.succeed(MagnitudeRepository(collection))

    override def get(collection: DocumentCollection, value: Magnitude): Task[Option[Magnitude]] =
      collection.get[Document](Key.safe(value.key))

    override def get(repository: MagnitudeRepository, value: Magnitude): Task[Option[Magnitude]] =
      repository.get(value.key)

    override def insert(repository: MagnitudeRepository, value: Magnitude): Task[Any] =
      repository.add(value)

    override def remove(repository: MagnitudeRepository, value: Magnitude): Task[Any] =
      repository.remove(value.key)

    override def update(repository: MagnitudeRepository, originalValue: Magnitude, updateValue: Any): Task[Any] =
      ???
