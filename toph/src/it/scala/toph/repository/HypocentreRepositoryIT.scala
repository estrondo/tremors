package toph.repository

import farango.DocumentCollection
import farango.data.Key
import farango.zio.given
import testkit.zio.repository.RepositoryIT
import toph.IT
import toph.fixture.HypocentreFixture
import toph.fixture.Point3DFixture
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

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("A HypocentreRepository")(
      suite("With Arango's container")(
        test("It should insert a hypocentre into collection.") {
          RepositoryIT.testAdd(HypocentreFixture.createRandom())
        },
        test("It should remove a hypocentre from collection.") {
          RepositoryIT.testRemove(HypocentreFixture.createRandom())
        },
        test("It should search for hypocentres by a spatial-epicentre-query.") {
          val epicentre = HypocentreFixture
            .createRandom()
            .copy(position = Point3DFixture.createRandom().copy(z = 3))

          val query = SpatialHypocentreQuery(
            boundary = Seq(epicentre.position.lng + .5, epicentre.position.lat + .5),
            boundaryRadius = Some(150000),
            minMagnitude = Some(1),
            maxMagnitude = Some(5),
            startTime = Some(epicentre.time.minusDays(3)),
            endTime = Some(epicentre.time.plusDays(3)),
            minDepth = Some(1),
            maxDepth = Some(4)
          )

          testHypocentreQuery(Seq(epicentre), query, Seq(epicentre))
        } @@ TestAspect.ignore,
        test("It should not found hypocentres by some spatial-epicentre-queries.") {
          val hypocentre = HypocentreFixture
            .createRandom()
            .copy(position = Point3DFixture.createRandom().copy(z = 3))

          val query = SpatialHypocentreQuery(
            boundary = Seq(hypocentre.position.lng + .5, hypocentre.position.lat + .5),
            boundaryRadius = Some(15000),
            minMagnitude = Some(1),
            maxMagnitude = Some(5),
            startTime = Some(hypocentre.time.minusDays(3)),
            endTime = Some(hypocentre.time.plusDays(3)),
            minDepth = Some(1),
            maxDepth = Some(4)
          )
          testHypocentreQuery(Seq(hypocentre), query, Nil)
        } @@ TestAspect.ignore
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
