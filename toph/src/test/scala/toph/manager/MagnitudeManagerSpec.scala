package toph.manager

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import testkit.quakeml.{MagnitudeFixture => QMagnitudeFixture}
import toph.Spec
import toph.converter.MagnitudeConverter
import toph.fixture.MagnitudeFixture
import toph.repository.MagnitudeRepository
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue

object MagnitudeManagerSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A MagnitudeManager")(
      test("It should add a magnitude.") {
        val magnitude = QMagnitudeFixture.createRandom()
        for
          expectedMagnitude <- MagnitudeConverter.fromQMagnitude(magnitude)
          _                 <- SweetMockitoLayer[MagnitudeRepository]
                                 .whenF2(_.add(expectedMagnitude))
                                 .thenReturn(expectedMagnitude)
          manager           <- ZIO.service[MagnitudeManager]
          result            <- manager.accept(magnitude)
        yield assertTrue(
          result == expectedMagnitude
        )
      }
    ).provideSome(
      SweetMockitoLayer.newMockLayer[MagnitudeRepository],
      ZLayer {
        ZIO.serviceWith[MagnitudeRepository](MagnitudeManager.apply)
      }
    )
