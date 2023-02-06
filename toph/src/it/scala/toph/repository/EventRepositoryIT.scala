package toph.repository

import core.KeyGenerator
import farango.DocumentCollection
import testkit.zio.testcontainers.ArangoDBLayer
import testkit.zio.testcontainers.FarangoLayer
import toph.fixture.EventFixture
import toph.repository.EventRepository
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.assertTrue
import zio.test.TestEnvironment
import toph.model.Event
import toph.IT
import EventRepository.given
import zio.test.TestAspect
import testkit.zio.repository.RepositoryIT
import zio.Task
import EventRepository.Document
import farango.zio.given

object EventRepositoryIT extends IT:

  given RepositoryIT[EventRepository, Event] with

    override def create(collection: DocumentCollection): Task[EventRepository]         =
      ZIO.succeed(EventRepository(collection))
    override def get(collection: DocumentCollection, key: String): Task[Option[Event]] = collection.get[Document](key)
    override def getKey(value: Event): String                                          = value.key
    override def insert(repository: EventRepository, value: Event): Task[Any]          = repository.add(value)
    override def remove(repository: EventRepository, value: Event): Task[Any]          = repository.remove(value.key)
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

  private val EventREpositoryLayer = ZLayer {
    ZIO.serviceWith[DocumentCollection](EventRepository(_))
  }
