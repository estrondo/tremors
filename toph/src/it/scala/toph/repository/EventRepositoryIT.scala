package toph.repository

import EventRepository.Document
import farango.DocumentCollection
import farango.data.Key
import farango.zio.given
import testkit.zio.repository.RepositoryIT
import toph.IT
import toph.fixture.EventFixture
import toph.geom.CoordinateSequenceFactory
import toph.geom.create
import toph.model.Event
import toph.query.EventQuery
import zio.RIO
import zio.Scope
import zio.Task
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.TestResult
import zio.test.assertTrue

object EventRepositoryIT extends IT:

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("A QueriableEventRepository")(
      test("It should add a queriable-event.") {
        RepositoryIT.testAdd(EventFixture.createRandom())
      },
      test("It should remove a queriable-event.") {
        RepositoryIT.testRemove(EventFixture.createRandom())
      },
      test("It should find an event.") {
        val event = EventFixture.createRandom()
        val point = event.position.get

        val query = EventQuery(
          boundary = Some(CoordinateSequenceFactory.create(point.getX() + .1, point.getY() + .1)),
          boundaryRadius = Some(30000),
          startTime = Some(event.time.get.minusDays(1)),
          endTime = Some(event.time.get.plusDays(1)),
          minDepth = Some(event.depth.get - 1500),
          maxDepth = Some(event.depth.get + 20000),
          minMagnitude = Some(event.magnitude.get - 0.5),
          maxMagnitude = Some(event.magnitude.get + 3),
          magnitudeType = Some(Set.from(event.magnitudeType))
        )

        testQuery(query, Seq(event), Seq(event))
      },
      test("It should not find any event.") {
        val event = EventFixture.createRandom()
        val point = event.position.get

        val query = EventQuery(
          boundary = Some(CoordinateSequenceFactory.create(point.getX() + .1, point.getY() + .1)),
          boundaryRadius = Some(7000),
          startTime = Some(event.time.get.minusDays(1)),
          endTime = Some(event.time.get.plusDays(1)),
          minDepth = Some(event.depth.get - 1500),
          maxDepth = Some(event.depth.get + 20000),
          minMagnitude = Some(event.magnitude.get - 0.5),
          maxMagnitude = Some(event.magnitude.get + 3),
          magnitudeType = Some(Set("@@@@"))
        )

        testQuery(query, Seq(event), Nil)
      }
    ).provideSome(
      RepositoryIT.of[EventRepository, Event]
    ) @@ TestAspect.sequential

  private def testQuery(
      query: EventQuery,
      input: Seq[Event],
      expected: Seq[Event]
  ): RIO[EventRepository, TestResult] =
    for
      repository <- RepositoryIT.insertAndReturnRepo(input)
      result     <- repository.search(query).runCollect
    yield assertTrue(
      result == expected
    )

  private given RepositoryIT[EventRepository, Event] with

    override def create(collection: DocumentCollection): Task[EventRepository] =
      EventRepository(collection)

    override def get(collection: DocumentCollection, value: Event): Task[Option[Event]] =
      collection.get[Document](Key.safe(value.key))

    override def get(repository: EventRepository, value: Event): Task[Option[Event]] =
      ???

    override def insert(repository: EventRepository, value: Event): Task[Any] =
      repository.add(value)

    override def remove(repository: EventRepository, value: Event): Task[Any] =
      repository.remove(value.key)

    override def update(
        repository: EventRepository,
        originalValue: Event,
        updateValue: Any
    ): Task[Any] = ???
