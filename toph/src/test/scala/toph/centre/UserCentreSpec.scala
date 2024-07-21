package toph.centre

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.Mockito.verify
import toph.TophSpec
import toph.model.AccountFixture
import toph.repository.AccountRepository
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object UserCentreSpec extends TophSpec:

  override def spec = suite("UserCentreSpec")(
    test(s"An UseCentre should invoke ${classOf[AccountRepository]}.update.") {
      val expectedUser = AccountFixture.createRandom()

      for
        _    <- SweetMockitoLayer[AccountRepository]
                  .whenF2(_.update(expectedUser.key, AccountRepository.Update("Albert")))
                  .thenReturn(expectedUser)
        user <- ZIO.serviceWithZIO[AccountService](_.update(expectedUser.key, AccountService.Update("Albert")))
      yield assertTrue(
        user == expectedUser
      )
    },
    test(s"Am UserCentre should invoke ${classOf[AccountRepository]}.add.") {
      val expectedAccount = AccountFixture.createRandom()
      for
        _          <- SweetMockitoLayer[AccountRepository].whenF2(_.add(expectedAccount)).thenReturn(expectedAccount)
        result     <- ZIO.serviceWithZIO[AccountService](_.add(expectedAccount))
        repository <- ZIO.service[AccountRepository]
      yield assertTrue(
        result == expectedAccount,
        verify(repository).add(expectedAccount) == null
      )
    }
  ).provideSome(
    SweetMockitoLayer.newMockLayer[AccountRepository],
    ZLayer {
      ZIO.serviceWith[AccountRepository](AccountService.apply)
    }
  )
