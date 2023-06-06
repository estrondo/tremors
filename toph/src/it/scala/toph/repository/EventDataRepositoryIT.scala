package toph.repository

import farango.DocumentCollection
import farango.data.Key
import farango.zio.given
import testkit.zio.repository.RepositoryIT
import toph.IT
import toph.fixture.EventDataFixture
import toph.model.data.EventData
import toph.repository.EventDataRepository.Document
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestEnvironment

object EventDataRepositoryIT extends IT:

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("An EventRepository")(
      suite("With Arango's container")(
        test("It should add a event into collection.") {
          RepositoryIT.testAdd(EventDataFixture.createRandom())
        },
        test("It should remove a event from collection.") {
          RepositoryIT.testRemove(EventDataFixture.createRandom())
        }
      ).provideSome(
        RepositoryIT.of[EventDataRepository, EventData]
      ) @@ TestAspect.sequential
    )

  given RepositoryIT[EventDataRepository, EventData] with

    override def create(collection: DocumentCollection): Task[EventDataRepository] =
      ZIO.succeed(EventDataRepository(collection))

    override def get(collection: DocumentCollection, value: EventData): Task[Option[EventData]] =
      collection.get[Document](Key.safe(value.key))

    override def get(repository: EventDataRepository, value: EventData): Task[Option[EventData]] =
      ???

    override def insert(repository: EventDataRepository, value: EventData): Task[Any] = repository.add(value)

    override def remove(repository: EventDataRepository, value: EventData): Task[Any] = repository.remove(value.key)

    override def update(repository: EventDataRepository, originalValue: EventData, updateValue: Any): Task[Any] =
      ???
