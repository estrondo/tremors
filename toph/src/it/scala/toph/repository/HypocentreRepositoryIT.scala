package toph.repository

import farango.DocumentCollection
import farango.data.Key
import farango.zio.given
import testkit.zio.repository.RepositoryIT
import toph.IT
import toph.fixture.HypocentreFixture
import toph.fixture.PointFixture
import toph.geom.CoordinateSequenceFactory
import toph.geom.create
import toph.model.Hypocentre
import toph.query.spatial.SpatialHypocentreQuery
import zio.RIO
import zio.Scope
import zio.Task
import zio.ZIO
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.TestResult
import zio.test.assertTrue

import HypocentreRepository.given
import HypocentreRepository.Document

object HypocentreRepositoryIT extends IT:

  given RepositoryIT[HypocentreRepository, Hypocentre] with
    override def create(collection: DocumentCollection): Task[HypocentreRepository] =
      HypocentreRepository(collection)

    override def get(collection: DocumentCollection, value: Hypocentre): Task[Option[Hypocentre]] =
      collection.get[Document](Key.safe(value.key))

    override def insert(repository: HypocentreRepository, value: Hypocentre): Task[Any] =
      repository.add(value)

    override def remove(repository: HypocentreRepository, value: Hypocentre): Task[Any] =
      repository.remove(value.key)

    override def update(repository: HypocentreRepository, originalValue: Hypocentre, updateValue: Any): Task[Any] = 
      ???

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("A HypocentreRepository")(
      suite("With Arango's container")(
        test("It should insert a hypocentre into collection.") {
          RepositoryIT.testAdd(HypocentreFixture.createRandom())
        },
        test("It should remove a hypocentre from collection.") {
          RepositoryIT.testRemove(HypocentreFixture.createRandom())
        },
        test("It should search for hypocentres by a spatial-hypocentre-query.") {
          val hypocentre = HypocentreFixture
            .createRandom()
            .copy(depth = 13456)

          val query = SpatialHypocentreQuery(
            boundary =
              CoordinateSequenceFactory.create(hypocentre.position.getX() + .1, hypocentre.position.getY() + .1),
            boundaryRadius = Some(30000),
            minMagnitude = Some(1),
            maxMagnitude = Some(5),
            startTime = Some(hypocentre.time.minusDays(3)),
            endTime = Some(hypocentre.time.plusDays(3)),
            minDepth = Some(10000),
            maxDepth = Some(20000)
          )

          testHypocentreQuery(Seq(hypocentre), query, Seq(hypocentre))
        },
        test("It should not found hypocentres by some spatial-hypocentre-queries.") {
          val hypocentre = HypocentreFixture
            .createRandom()
            .copy(position = PointFixture.createRandom())

          val query = SpatialHypocentreQuery(
            boundary =
              CoordinateSequenceFactory.create(hypocentre.position.getX() + .5, hypocentre.position.getY() + .5),
            boundaryRadius = Some(15000),
            minMagnitude = Some(1),
            maxMagnitude = Some(5),
            startTime = Some(hypocentre.time.minusDays(3)),
            endTime = Some(hypocentre.time.plusDays(3)),
            minDepth = Some(1),
            maxDepth = Some(4)
          )
          testHypocentreQuery(Seq(hypocentre), query, Nil)
        }
      ).provideSomeLayer(
        RepositoryIT.of[HypocentreRepository, Hypocentre]
      ) @@ TestAspect.sequential
    )

  private def testHypocentreQuery(
      input: Seq[Hypocentre],
      query: SpatialHypocentreQuery,
      expected: Seq[Hypocentre]
  ): RIO[HypocentreRepository, TestResult] =
    for
      repository <- RepositoryIT.insertAndReturnRepo(input)
      result     <- repository.query(query).runCollect
    yield assertTrue(
      result == expected
    )
