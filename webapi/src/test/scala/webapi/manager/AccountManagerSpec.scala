package webapi.manager

import core.KeyGenerator
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import webapi.Spec
import webapi.fixture.AccountFixture
import webapi.model.Account
import webapi.repository.AccountRepository
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue

object AccountManagerSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A AccountManager")(
      test("It should add an account.") {
        val expectedUser = AccountFixture.createRandom()
        for
          _      <- SweetMockitoLayer[AccountRepository]
                      .whenF2(_.add(expectedUser))
                      .thenReturn(expectedUser)
          result <- ZIO.serviceWithZIO[AccountManager](_.add(expectedUser))
        yield assertTrue(
          result == expectedUser
        )
      },
      test("It should get an account by email.") {
        val expectedUser = AccountFixture.createRandom()
        for
          _      <- SweetMockitoLayer[AccountRepository]
                      .whenF2(_.get(expectedUser.email))
                      .thenReturn(Some(expectedUser))
          result <- ZIO.serviceWithZIO[AccountManager](_.get(expectedUser.email))
        yield assertTrue(
          result == Some(expectedUser)
        )
      },
      test("It should update an account.") {
        val account = AccountFixture.createRandom()
        val update  = Account.Update(name = "Tesla")

        for
          _      <- SweetMockitoLayer[AccountRepository]
                      .whenF2(_.update(account.email, update))
                      .thenReturn(Some(account))
          result <- ZIO.serviceWithZIO[AccountManager](_.update(account.email, update))
        yield assertTrue(
          result == Some(account)
        )
      },
      test("It should remove an account.") {
        val account = AccountFixture.createRandom()

        for
          _      <- SweetMockitoLayer[AccountRepository]
                      .whenF2(_.remove(account.email))
                      .thenReturn(Some(account))
          result <- ZIO.serviceWithZIO[AccountManager](_.remove(account.email))
        yield assertTrue(
          result == Some(account)
        )
      },
      test("It should activate an account.") {
        val account = AccountFixture.createRandom()

        for
          _      <- SweetMockitoLayer[AccountRepository]
                      .whenF2(_.activate(account.email))
                      .thenReturn(Some(account))
          _      <- SweetMockitoLayer[AccountRepository]
                      .whenF2(_.get(account.email))
                      .thenReturn(Some(account.copy(active = false)))
          result <- ZIO
                      .serviceWithZIO[AccountManager](_.activate(account.email, account.secret))
                      .some
        yield assertTrue(
          result == account
        )
      },
      test("It should refuse to activate an account with a wrong code.") {
        val account = AccountFixture.createRandom()

        for
          _      <- SweetMockitoLayer[AccountRepository]
                      .whenF2(_.activate(account.email))
                      .thenReturn(Some(account))
          _      <- SweetMockitoLayer[AccountRepository]
                      .whenF2(_.get(account.email))
                      .thenReturn(Some(account.copy(active = false)))
          result <- ZIO
                      .serviceWithZIO[AccountManager](_.activate(account.email, KeyGenerator.next64()))
        yield assertTrue(
          result == None
        )
      }
    ).provideSome(
      SweetMockitoLayer.newMockLayer[AccountRepository],
      ZLayer(ZIO.serviceWith[AccountRepository](AccountManager.apply))
    )
