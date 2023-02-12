package toph.manager

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers.any
import quakeml.DetectedEvent
import testkit.core.createZonedDateTime
import testkit.quakeml.{EventFixture => QEventFixture}
import toph.Spec
import toph.converter.EventConverter
import toph.fixture.EpicentreFixture
import toph.fixture.HypocentreFixture
import toph.repository.EventRepository
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue

object EventManagerSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("An EventManager")(
      test("It should accept a dected event and notify all associeted components.") {
        val now           = createZonedDateTime()
        val originalEvent = QEventFixture.createRandom()
        val detectedEvent = DetectedEvent(now, originalEvent)
        val epicentre     = EpicentreFixture.createRandom()
        val hypocentre    = HypocentreFixture.createRandom()

        for
          expectedEvent <- EventConverter.fromQEvent(originalEvent)
          manager       <- ZIO.service[EventManager]
          _             <- SweetMockitoLayer[EventRepository]
                             .whenF2(_.add(expectedEvent))
                             .thenReturn(expectedEvent)
          _             <- SweetMockitoLayer[SpatialManager]
                             .whenF2(_.accept(any()))
                             .thenReturn((epicentre, Some(hypocentre)))
          accepted      <- manager.accept(detectedEvent)
        yield assertTrue(
          accepted == (expectedEvent, Seq((epicentre, Some(hypocentre))))
        )
      }
    ).provideSome(
      SweetMockitoLayer.newMockLayer[EventRepository],
      SweetMockitoLayer.newMockLayer[SpatialManager],
      SweetMockitoLayer.newMockLayer[MagnitudeManager],
      ZLayer {
        for
          repository       <- ZIO.service[EventRepository]
          spatialManager   <- ZIO.service[SpatialManager]
          magnitudeManager <- ZIO.service[MagnitudeManager]
        yield EventManager(repository, spatialManager, magnitudeManager)
      }
    )
