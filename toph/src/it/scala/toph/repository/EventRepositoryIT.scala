package toph.repository

import core.KeyGenerator
import farango.DocumentCollection
import farango.zio.given
import testkit.zio.repository.RepositoryIT
import testkit.zio.testcontainers.ArangoDBLayer
import testkit.zio.testcontainers.FarangoLayer
import toph.IT
import toph.fixture.EventFixture
import toph.model.Event
import toph.repository.EventRepository
import toph.repository.EventRepository.Document
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.assertTrue
import farango.data.Key

object EventRepositoryIT extends IT:

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("An EventRepository")(
      suite("With Arango's container")(
        test("It should add a event into collection.") {
          RepositoryIT.testAdd(EventFixture.createRandom())
        },
        test("It should remove a event from collection.") {
          RepositoryIT.testRemove(EventFixture.createRandom())
        }
      ).provideSome(
        RepositoryIT.of[EventRepository, Event]
      ) @@ TestAspect.sequential
    )

  given RepositoryIT[EventRepository, Event] with

    override def create(collection: DocumentCollection): Task[EventRepository]          =
      ZIO.succeed(EventRepository(collection))
    override def get(collection: DocumentCollection, value: Event): Task[Option[Event]] =
      collection.get[Document](Key.safe(value.key))

    override def insert(repository: EventRepository, value: Event): Task[Any] = repository.add(value)

    override def remove(repository: EventRepository, value: Event): Task[Any] = repository.remove(value.key)

    override def update(repository: EventRepository, originalValue: Event, updateValue: Any): Task[Any] = 
      ???
