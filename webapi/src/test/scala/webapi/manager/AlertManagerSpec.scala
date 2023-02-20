package webapi.manager

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import webapi.Spec
import webapi.WebAPIException
import webapi.fixture.AlertFixture
import webapi.fixture.UserFixture
import webapi.repository.AlertRepository
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Assertion
import zio.test.TestEnvironment
import zio.test.assert
import zio.test.assertTrue
import webapi.model.Alert
import zio.stream.ZStream

object AlertManagerSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("An AlertManager")(
      test("It should add a new alert.") {
        val alert = AlertFixture.createRandom()
        val user  = UserFixture.createRandom().copy(email = alert.email)
        for
          _      <- SweetMockitoLayer[UserManager]
                      .whenF2(_.get(alert.email))
                      .thenReturn(Some(user))
          _      <- SweetMockitoLayer[AlertRepository]
                      .whenF2(_.add(alert))
                      .thenReturn(alert)
          result <- ZIO.serviceWithZIO[AlertManager](_.add(alert))
        yield assertTrue(
          result == alert
        )
      },
      test("It should fail when the user was not found.") {
        val alert = AlertFixture.createRandom()
        for
          _    <- SweetMockitoLayer[UserManager]
                    .whenF2(_.get(alert.email))
                    .thenReturn(None)
          exit <- ZIO.serviceWithZIO[AlertManager](_.add(alert)).exit
        yield assert(exit)(
          Assertion.fails(Assertion.isSubtype[WebAPIException.Invalid](Assertion.anything))
        )
      },
      test("It should update an alert.") {
        val alert  = AlertFixture.createRandom()
        val update = Alert.Update(
          area = None,
          areaRadius = None,
          magnitudeFilter = Nil,
          location = Nil
        )

        for
          _      <- SweetMockitoLayer[AlertRepository]
                      .whenF2(_.update(alert.key, update))
                      .thenReturn(Some(alert))
          result <- ZIO.serviceWithZIO[AlertManager](_.update(alert.key, update))
        yield assertTrue(
          result == Some(alert)
        )
      },
      test("It should remove an alert.") {
        val alert = AlertFixture.createRandom()
        for
          _      <- SweetMockitoLayer[AlertRepository]
                      .whenF2(_.remove(alert.key))
                      .thenReturn(Some(alert))
          result <- ZIO.serviceWithZIO[AlertManager](_.remove(alert.key))
        yield assertTrue(
          result == Some(alert)
        )
      },
      test("It should list all alerts.") {
        val alerts = (0 until 10).map(_ => AlertFixture.createRandom())
        for
          _      <- SweetMockitoLayer[AlertRepository]
                      .whenF2(_.all())
                      .thenReturn(ZStream.fromIterable(alerts))
          stream <- ZIO.serviceWithZIO[AlertManager](_.all())
          result <- stream.runCollect
        yield assertTrue(
          result.toSet == alerts.toSet
        )
      },
      test("It should disable an alert.") {
        val alert = AlertFixture.createRandom().copy(enabled = false)
        for
          _      <- SweetMockitoLayer[AlertRepository]
                      .whenF2(_.enable(alert.key, false))
                      .thenReturn(Some(alert))
          result <- ZIO.serviceWithZIO[AlertManager](_.enable(alert.key, false))
        yield assertTrue(
          result == Some(alert)
        )
      },
      test("It should enable an alert.") {
        val alert = AlertFixture.createRandom().copy(enabled = true)
        for
          _      <- SweetMockitoLayer[AlertRepository]
                      .whenF2(_.enable(alert.key, true))
                      .thenReturn(Some(alert))
          result <- ZIO.serviceWithZIO[AlertManager](_.enable(alert.key, true))
        yield assertTrue(
          result == Some(alert)
        )
      }
    ).provideSome(
      SweetMockitoLayer.newMockLayer[AlertRepository],
      SweetMockitoLayer.newMockLayer[UserManager],
      ZLayer(ZIO.serviceWith[AlertRepository](AlertManager.apply))
    )
