package toph.manager

import one.estrondo.sweetmockito.Answer
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers.any
import quakeml.QuakeMLDetectedEvent
import testkit.core.createZonedDateTime
import testkit.quakeml.{QuakeMLEventFixture => QEventFixture}
import toph.Spec
import toph.converter.EventDataConverter
import toph.converter.MagnitudeDataConverter
import toph.fixture.HypocentreDataFixture
import toph.repository.EventDataRepository
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue

object EventDataManagerSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("An EventManager")(
      test("It should accept a dected event and notify all associeted components.") {
        val now           = createZonedDateTime()
        val originalEvent = QEventFixture.createRandom()
        val detectedEvent = QuakeMLDetectedEvent(now, originalEvent)
        val hypocentre    = HypocentreDataFixture.createRandom()

        for
          expectedEvent      <- EventDataConverter.fromQEvent(originalEvent)
          convertedMagnitude <- MagnitudeDataConverter.fromQMagnitude(originalEvent.magnitude.head)
          manager            <- ZIO.service[EventDataManager]
          _                  <- SweetMockitoLayer[EventDataRepository]
                                  .whenF2(_.add(expectedEvent))
                                  .thenReturn(expectedEvent)
          _                  <- SweetMockitoLayer[SpatialManager]
                                  .whenF2(_.accept(originalEvent.origin.head))
                                  .thenReturn(hypocentre)
          _                  <- SweetMockitoLayer[MagnitudeDataManager]
                                  .whenF2(_.accept(originalEvent.magnitude.head))
                                  .thenReturn(convertedMagnitude)
          accepted           <- manager.accept(detectedEvent)
        yield assertTrue(
          accepted == (expectedEvent, Seq(hypocentre), Seq(convertedMagnitude))
        )
      }
    ).provideSome(
      SweetMockitoLayer.newMockLayer[EventDataRepository],
      SweetMockitoLayer.newMockLayer[SpatialManager],
      SweetMockitoLayer.newMockLayer[MagnitudeDataManager],
      ZLayer {
        for
          repository       <- ZIO.service[EventDataRepository]
          spatialManager   <- ZIO.service[SpatialManager]
          magnitudeManager <- ZIO.service[MagnitudeDataManager]
        yield EventDataManager(repository, spatialManager, magnitudeManager)
      }
    )
