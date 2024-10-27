package toph.service

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.Mockito.verify
import toph.TophSpec
import toph.model.AccountFixture
import toph.repository.AccountRepository
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object AccountServiceSpec extends TophSpec:

  private val accountRepositoryClassName = classOf[AccountRepository].getCanonicalName

  override def spec = suite("The AccountService")(
    test(s"It should invoke $accountRepositoryClassName's update.") {
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
    test(s"It should invoke $accountRepositoryClassName's add.") {
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
  ).provideSome(
    SweetMockitoLayer.newMockLayer[AccountRepository],
    ZLayer {
      ZIO.serviceWith[AccountRepository](AccountService.apply)
    },
  )
