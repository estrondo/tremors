package toph.repository

import farango.DocumentCollection
import farango.data.Key
import farango.zio.given
import testkit.zio.repository.RepositoryIT
import toph.IT
import toph.fixture.EpicentreFixture
import toph.geom.CoordinateSequenceFactory
import toph.geom.create
import toph.model.Epicentre
import toph.query.spatial.SpatialEpicentreQuery
import zio.RIO
import zio.Scope
import zio.Task
import zio.ZIO
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.TestResult
import zio.test.assertTrue

import EpicentreRepository.Document

object EpicentreRepositoryIT extends IT:

  given RepositoryIT[EpicentreRepository, Epicentre] with
    override def create(collection: DocumentCollection): Task[EpicentreRepository] =
      EpicentreRepository(collection)

    override def get(collection: DocumentCollection, value: Epicentre): Task[Option[Epicentre]] =
      collection.get[Document](Key.safe(value.key))

    override def insert(repository: EpicentreRepository, value: Epicentre): Task[Any] =
      repository.add(value)

    override def remove(repository: EpicentreRepository, value: Epicentre): Task[Any] =
      repository.remove(value.key)

    override def update(repository: EpicentreRepository, originalValue: Epicentre, updateValue: Any): Task[Any] = 
      ???

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("An EpicentreRepository")(
      suite("With Arango's container")(
        test("It should add a new epicentre into collection.") {
          RepositoryIT.testAdd(EpicentreFixture.createRandom())
        },
        test("It should remove a epicentre from collection.") {
          RepositoryIT.testRemove(EpicentreFixture.createRandom())
        },
        test("It should search for epicentres by a spatial-epicentre-query.") {
          val epicentre = EpicentreFixture.createRandom()

          val query = SpatialEpicentreQuery(
            boundary = CoordinateSequenceFactory.create(epicentre.position.getX() + .1, epicentre.position.getY() + .1),
            boundaryRadius = Some(30000),
            minMagnitude = Some(1),
            maxMagnitude = Some(5),
            startTime = Some(epicentre.time.minusDays(3)),
            endTime = Some(epicentre.time.plusDays(3))
          )

          testSpatialEpicentreQuery(Seq(epicentre), query, Seq(epicentre))
        },
        test("It should not found epicentres by some spatial-epicentre-queries.") {
          val epicentre = EpicentreFixture.createRandom()

          val query = SpatialEpicentreQuery(
            boundary = CoordinateSequenceFactory.create(epicentre.position.getX() + .5, epicentre.position.getY() + .5),
            boundaryRadius = Some(15000),
            minMagnitude = Some(1),
            maxMagnitude = Some(5),
            startTime = Some(epicentre.time.minusDays(3)),
            endTime = Some(epicentre.time.plusDays(3))
          )
          testSpatialEpicentreQuery(Seq(epicentre), query, Nil)
        }
      ).provideSomeLayer(
        RepositoryIT.of[EpicentreRepository, Epicentre]
      ) @@ TestAspect.sequential
    )

  private def testSpatialEpicentreQuery(
      input: Seq[Epicentre],
      query: SpatialEpicentreQuery,
      expected: Seq[Epicentre]
  ): RIO[EpicentreRepository, TestResult] =
    for
      repository <- RepositoryIT.insertAndReturnRepo(input)
      result     <- repository.query(query).runCollect
    yield assertTrue(
      result == expected
    )
