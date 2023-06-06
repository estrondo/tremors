package toph.manager

import core.KeyGenerator
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.Mockito
import testkit.quakeml.QuakeMLOriginFixture
import toph.Spec
import toph.converter.HypocentreDataConverter
import toph.fixture.EventDataFixture
import toph.fixture.EventFixture
import toph.fixture.HypocentreDataFixture
import toph.fixture.MagnitudeDataFixture
import toph.model.Event
import toph.model.Uncertainty2D
import toph.query.EventQuery
import toph.repository.EventRepository
import toph.repository.HypocentreDataRepository
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue

object SpatialManagerSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A SpatialManager")(
      test("It should map corrrectly.") {
        val hypocentre            = HypocentreDataFixture.createRandom()
        val origin                = HypocentreDataFixture.updateOriginWith(QuakeMLOriginFixture.createRandom(), hypocentre)
        val hypocentreWithNoDepth = hypocentre.copy(depth = None, depthUncertainty = None)
        val originWithNoDepth     = HypocentreDataFixture.updateOriginWith(origin, hypocentreWithNoDepth)

        for
          convertedHypocentre            <- HypocentreDataConverter.from(origin)
          convertedHypocentreWithNoDepth <- HypocentreDataConverter.from(originWithNoDepth)
        yield assertTrue(
          convertedHypocentre == hypocentre,
          convertedHypocentreWithNoDepth == hypocentreWithNoDepth
        )
      },
      test("It should add hypocentre from an origin.") {
        val hypocentre = HypocentreDataFixture.createRandom()
        val origin     = HypocentreDataFixture.updateOriginWith(QuakeMLOriginFixture.createRandom(), hypocentre)

        for
          _      <- SweetMockitoLayer[HypocentreDataRepository]
                      .whenF2(_.add(hypocentre))
                      .thenReturn(hypocentre)
          result <- ZIO.serviceWithZIO[SpatialManager](_.accept(origin))
        yield assertTrue(
          result == hypocentre
        )
      },
      test("It should add hypocentre with no depth.") {
        val hypocentre = HypocentreDataFixture
          .createRandom()
          .copy(depth = None, depthUncertainty = None)

        val origin = HypocentreDataFixture
          .updateOriginWith(QuakeMLOriginFixture.createRandom(), hypocentre)

        for
          _      <- SweetMockitoLayer[HypocentreDataRepository]
                      .whenF2(_.add(hypocentre))
                      .thenReturn(hypocentre)
          result <- ZIO.serviceWithZIO[SpatialManager](_.accept(origin))
        yield assertTrue(
          result == hypocentre
        )
      },
      test("It should create some queriable-events.") {
        val hypocentre = HypocentreDataFixture.createRandom()
        val magnitude  = MagnitudeDataFixture.createRandom()

        val event = EventDataFixture
          .createRandom()
          .copy(
            preferredOriginKey = Some(hypocentre.key),
            preferedMagnitudeKey = Some(magnitude.key),
            originKey = Seq(hypocentre.key),
            magnitudeKey = Seq(magnitude.key)
          )

        val queriableKey = KeyGenerator.next32()

        val queriableEvent = Event(
          key = queriableKey,
          eventKey = event.key,
          hypocentreKey = Some(hypocentre.key),
          magnitudeKey = Some(magnitude.key),
          eventType = event.`type`,
          position = Some(hypocentre.position),
          positionUncertainty = Some(hypocentre.positionUncertainty),
          depth = hypocentre.depth,
          depthUncertainty = hypocentre.depthUncertainty,
          time = Some(hypocentre.time),
          timeUncertainty = Some(hypocentre.timeUncertainty),
          stationCount = magnitude.stationCount,
          magnitude = Some(magnitude.mag),
          magnitudeType = magnitude.`type`,
          creationInfo = event.creationInfo
        )

        for
          _      <- ZIO.serviceWith[KeyGenerator](x => Mockito.when(x.next32()).thenReturn(queriableKey))
          _      <- SweetMockitoLayer[EventRepository]
                      .whenF2(_.add(queriableEvent))
                      .thenReturn(queriableEvent)
          result <- ZIO.serviceWithZIO[SpatialManager](
                      _.createEvents(event, Seq(hypocentre), Seq(magnitude))
                    )
        yield assertTrue(
          result == Seq(queriableEvent)
        )
      },
      test("It should search for queriable-events.") {
        val event = EventFixture.createRandom()
        val query = EventQuery(
          boundary = None,
          boundaryRadius = None,
          startTime = None,
          endTime = None,
          minDepth = None,
          maxDepth = None,
          minMagnitude = None,
          maxMagnitude = None,
          magnitudeType = None
        )

        for
          _      <- SweetMockitoLayer[EventRepository]
                      .whenF2(_.search(query))
                      .thenReturn(event)
          result <- ZIO.serviceWithZIO[SpatialManager](_.search(query).runCollect)
        yield assertTrue(
          result == Seq(event)
        )
      }
    ).provideSome(
      SweetMockitoLayer.newMockLayer[HypocentreDataRepository],
      SweetMockitoLayer.newMockLayer[EventRepository],
      SweetMockitoLayer.newMockLayer[KeyGenerator],
      ZLayer {
        for
          hypocentreRepository     <- ZIO.service[HypocentreDataRepository]
          queriableEventRepository <- ZIO.service[EventRepository]
          keyGenerator             <- ZIO.service[KeyGenerator]
        yield SpatialManager(hypocentreRepository, queriableEventRepository, keyGenerator)
      }
    )
