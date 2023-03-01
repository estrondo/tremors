package webapi.service

import core.KeyGenerator
import grpc.webapi.account.AccountKey
import grpc.webapi.account.AccountReponse
import grpc.webapi.account.AccountUpdate
import grpc.webapi.account.ZioAccount.AccountServiceClient
import grpc.webapi.account.ZioAccount.ZAccountService
import grpc.webapi.account.{Account => GRPCAccount}
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import scalapb.zio_grpc.RequestContext
import scalapb.zio_grpc.Server
import testkit.zio.grpc.GRPC
import webapi.IT
import webapi.fixture.AccountFixture
import webapi.manager.AccountManager
import webapi.model.Account
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.config.derivation.name
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestClock
import zio.test.TestEnvironment
import zio.test.assertTrue

import scala.annotation.newMain
import org.mockito.Mockito

object AccountServiceIT extends IT:

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("An AccountService")(
      test("It should create an account.") {
        val account = AccountFixture
          .createRandom()
          .copy(active = false)

        val request = GRPCAccount(
          email = account.email,
          name = account.name
        )

        for
          _       <- SweetMockitoLayer[AccountManager]
                       .whenF2(_.add(account))
                       .thenReturn(account)
          _       <- ZIO.serviceWith[KeyGenerator](m => Mockito.when(m.next4()).thenReturn(account.secret))
          channel <- GRPC.createChannel
          _       <- TestClock.setTime(account.createdAt.toInstant())
          client  <- AccountServiceClient.scoped(channel)
          result  <- client.create(request)
        yield assertTrue(
          result == AccountReponse(account.email)
        )
      },
      test("It should update an account.") {
        val account = AccountFixture.createRandom()

        val request = AccountUpdate(email = account.email, newName = Some(account.name + "@"))

        for
          channel <- GRPC.createChannel
          client  <- AccountServiceClient.scoped(channel)
          _       <- SweetMockitoLayer[AccountManager]
                       .whenF2(_.update(account.email, Account.Update(name = account.name + "@")))
                       .thenReturn(Some(account))
          result  <- client.update(request)
        yield assertTrue(
          result == AccountReponse(account.email)
        )
      },
      test("It should remove an account.") {
        val account = AccountFixture.createRandom()
        val request = AccountKey(email = account.email)

        for
          channel <- GRPC.createChannel
          client  <- AccountServiceClient.scoped(channel)
          _       <- SweetMockitoLayer[AccountManager]
                       .whenF2(_.remove(account.email))
                       .thenReturn(Some(account))
          result  <- client.remove(request)
        yield assertTrue(
          result == AccountReponse(account.email)
        )
      }
    ).provideSome[Scope](
      SweetMockitoLayer.newMockLayer[AccountManager],
      SweetMockitoLayer.newMockLayer[KeyGenerator],
      GRPC.serverLayerFor[ZAccountService[RequestContext]],
      ZLayer(AccountService())
    ) @@ TestAspect.sequential
