package toph.repository

import farango.DocumentCollection
import farango.zio.given
import testkit.zio.repository.RepositoryIT
import toph.IT
import toph.fixture.EpicentreFixture
import toph.model.Epicentre
import zio.Scope
import zio.Task
import zio.ZIO
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestEnvironment

import EpicentreRepository.Document

object EpicentreRepositoryIT extends IT:

  given RepositoryIT[EpicentreRepository, Epicentre] with
    override def create(collection: DocumentCollection): Task[EpicentreRepository] =
      EpicentreRepository(collection)

    override def get(collection: DocumentCollection, value: Epicentre): Task[Option[Epicentre]] =
      collection.get[Document](value.key)

    override def insert(repository: EpicentreRepository, value: Epicentre): Task[Any] =
      repository.add(value)

    override def remove(repository: EpicentreRepository, value: Epicentre): Task[Any] =
      repository.remove(value.key)

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("An EpicentreRepository")(
      suite("With Arango's container")(
        test("It should add a new epicentre into collection.") {
          RepositoryIT.testAdd(EpicentreFixture.createRandom())
        },
        test("It should remove a epicentre from collection.") {
          RepositoryIT.testRemove(EpicentreFixture.createRandom())
        }
      ).provideSomeLayer(
        RepositoryIT.of[EpicentreRepository, Epicentre]
      ) @@ TestAspect.sequential
    )
