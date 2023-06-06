package toph.manager

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import testkit.quakeml.{QuakeMLMagnitudeFixture => QMagnitudeFixture}
import toph.Spec
import toph.converter.MagnitudeDataConverter
import toph.repository.MagnitudeDataRepository
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue

object MagnitudeDataManagerSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A MagnitudeManager")(
      test("It should add a magnitude.") {
        val magnitude = QMagnitudeFixture.createRandom()
        for
          expectedMagnitude <- MagnitudeDataConverter.fromQMagnitude(magnitude)
          _                 <- SweetMockitoLayer[MagnitudeDataRepository]
                                 .whenF2(_.add(expectedMagnitude))
                                 .thenReturn(expectedMagnitude)
          manager           <- ZIO.service[MagnitudeDataManager]
          result            <- manager.accept(magnitude)
        yield assertTrue(
          result == expectedMagnitude
        )
      }
    ).provideSome(
      SweetMockitoLayer.newMockLayer[MagnitudeDataRepository],
      ZLayer {
        ZIO.serviceWith[MagnitudeDataRepository](MagnitudeDataManager.apply)
      }
    )
