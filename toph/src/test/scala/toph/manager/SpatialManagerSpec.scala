package toph.manager

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import testkit.core.createZonedDateTime
import testkit.quakeml.OriginFixture
import testkit.quakeml.RealQuantityFixture
import toph.Spec
import toph.converter.EpicentreConverter
import toph.converter.HypocentreConverter
import toph.fixture.EpicentreFixture
import toph.fixture.HypocentreFixture
import toph.model.Epicentre
import toph.model.Point2D
import toph.model.Uncertainty2D
import toph.query.spatial.SpatialEpicentreQuery
import toph.query.spatial.SpatialHypocentreQuery
import toph.repository.EpicentreRepository
import toph.repository.HypocentreRepository
import zio.Chunk
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue

object SpatialManagerSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A SpatialManager")(
      test("It should map corrrectly.") {
        val hypocentre = HypocentreFixture.createRandom()
        val epicentre  = EpicentreFixture.from(hypocentre)
        val origin     = HypocentreFixture.updateWith(OriginFixture.createRandom(), hypocentre)
        for
          convertedEpicentre  <- EpicentreConverter.from(origin)
          convertedHypocentre <- HypocentreConverter.from(origin)
        yield assertTrue(
          convertedEpicentre == epicentre,
          convertedHypocentre == Some(hypocentre)
        )
      },
      test("It should add epicentre and hypocentre from a origin with both.") {
        val hypocentre = HypocentreFixture.createRandom()
        val epicentre  = EpicentreFixture.from(hypocentre)
        val origin     = HypocentreFixture.updateWith(OriginFixture.createRandom(), hypocentre)

        for
          manager <- ZIO.service[SpatialManager]
          _       <- SweetMockitoLayer[EpicentreRepository]
                       .whenF2(_.add(epicentre))
                       .thenReturn(epicentre)
          _       <- SweetMockitoLayer[HypocentreRepository]
                       .whenF2(_.add(hypocentre))
                       .thenReturn(hypocentre)
          result  <- manager.accept(origin)
        yield assertTrue(
          result == (epicentre, Some(hypocentre))
        )
      },
      test("It should add just epicentre from an origin with no depth.") {
        val hypocentre = HypocentreFixture.createRandom()
        val epicentre  = EpicentreFixture.from(hypocentre)
        val origin     = HypocentreFixture
          .updateWith(OriginFixture.createRandom(), hypocentre)
          .copy(depth = None)

        for
          manager <- ZIO.service[SpatialManager]
          _       <- SweetMockitoLayer[EpicentreRepository]
                       .whenF2(_.add(epicentre))
                       .thenReturn(epicentre)
          result  <- manager.accept(origin)
        yield assertTrue(
          result == (epicentre, None)
        )
      },
      test("It should search for epicentres when receive a SpatialEpicentreQuery.") {
        val spatiaQuery = SpatialEpicentreQuery(
          boundary = Seq(-45, -21, -44, -20),
          boundaryRadius = Some(15000),
          minMagnitude = Some(1),
          maxMagnitude = Some(5),
          startTime = Some(createZonedDateTime()),
          endTime = Some(createZonedDateTime())
        )

        val epicentre = EpicentreFixture.createRandom()

        for
          _       <- SweetMockitoLayer[EpicentreRepository]
                       .whenF2(_.query(spatiaQuery))
                       .thenReturn(epicentre)
          manager <- ZIO.service[SpatialManager]
          result  <- manager.search(spatiaQuery).runCollect
        yield assertTrue(
          result == Chunk(epicentre)
        )
      },
      test("It should search for hypocentres when receive a SpatialHypocentreQuery.") {
        val spatiaQuery = SpatialHypocentreQuery(
          boundary = Seq(-45, -21, -44, -20),
          boundaryRadius = Some(15000),
          minMagnitude = Some(1),
          maxMagnitude = Some(5),
          startTime = Some(createZonedDateTime()),
          endTime = Some(createZonedDateTime()),
          minDepth = Some(1),
          maxDepth = Some(3)
        )

        val hypocentre = HypocentreFixture.createRandom()

        for
          _       <- SweetMockitoLayer[HypocentreRepository]
                       .whenF2(_.query(spatiaQuery))
                       .thenReturn(hypocentre)
          manager <- ZIO.service[SpatialManager]
          result  <- manager.search(spatiaQuery).runCollect
        yield assertTrue(
          result == Chunk(hypocentre)
        )
      }
    ).provideSome(
      SweetMockitoLayer.newMockLayer[EpicentreRepository],
      SweetMockitoLayer.newMockLayer[HypocentreRepository],
      ZLayer {
        for
          epicentreRepository  <- ZIO.service[EpicentreRepository]
          hypocentreRepository <- ZIO.service[HypocentreRepository]
        yield SpatialManager(epicentreRepository, hypocentreRepository)
      }
    )
