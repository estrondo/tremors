package toph.service

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import toph.TophSpec
import toph.model.AccountFixture
import toph.repository.AccountRepository
import tremors.generator.KeyGenerator
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object AccountServiceSpec extends TophSpec:

  private val accountRepositoryClassName = classOf[AccountRepository].getCanonicalName

  override def spec = suite("The AccountService")(
    test(s"It should invoke $accountRepositoryClassName' update.") {
      val expectedUser = AccountFixture.createRandom()

      for
        _    <- SweetMockitoLayer[AccountRepository]
                  .whenF2(_.update(expectedUser.key, AccountRepository.Update("Albert")))
                  .thenReturn(expectedUser)
        user <- ZIO.serviceWithZIO[AccountService](_.update(expectedUser.key, AccountService.Update("Albert")))
      yield assertTrue(
        user == expectedUser,
      )
    },
    test(s"It should invoke $accountRepositoryClassName' add.") {
      val expectedAccount = AccountFixture.createRandom()
      for
        _          <- SweetMockitoLayer[AccountRepository].whenF2(_.add(expectedAccount)).thenReturn(expectedAccount)
        result     <- ZIO.serviceWithZIO[AccountService](_.add(expectedAccount))
        repository <- ZIO.service[AccountRepository]
      yield assertTrue(
        result == expectedAccount,
        verify(repository).add(expectedAccount) == null,
      )
    },
    test(s"It should invoke $accountRepositoryClassName' searchEmail and add.") {
      val expectedAccount = AccountFixture.createRandom().copy(name = "")
      for
        _            <- SweetMockitoLayer[AccountRepository].whenF2(_.searchByEmail(expectedAccount.email)).thenReturn(None)
        _            <- SweetMockitoLayer[AccountRepository].whenF2(_.add(expectedAccount)).thenReturn(expectedAccount)
        keyGenerator <- ZIO.service[KeyGenerator]
        _             = when(keyGenerator.short()).thenReturn(expectedAccount.key)
        result       <- ZIO.serviceWithZIO[AccountService](_.findOrCreate(expectedAccount.email))
        repository   <- ZIO.service[AccountRepository]
      yield assertTrue(
        result == expectedAccount,
        verify(repository).searchByEmail(expectedAccount.email) == null,
        verify(repository).add(expectedAccount) == null,
      )
    },
    test(s"It should invoke $accountRepositoryClassName' searchEmail.") {
      val expectedAccount = AccountFixture.createRandom()
      for
        _          <- SweetMockitoLayer[AccountRepository]
                        .whenF2(_.searchByEmail(expectedAccount.email))
                        .thenReturn(Some(expectedAccount))
        result     <- ZIO.serviceWithZIO[AccountService](_.findOrCreate(expectedAccount.email))
        repository <- ZIO.service[AccountRepository]
      yield assertTrue(
        result == expectedAccount,
        verify(repository).searchByEmail(expectedAccount.email) == null,
      )
    },
  ).provideSome(
    SweetMockitoLayer.newMockLayer[AccountRepository],
    SweetMockitoLayer.newMockLayer[KeyGenerator],
    ZLayer {
      for
        repository   <- ZIO.service[AccountRepository]
        keyGenerator <- ZIO.service[KeyGenerator]
      yield AccountService(repository, keyGenerator)
    },
  )
