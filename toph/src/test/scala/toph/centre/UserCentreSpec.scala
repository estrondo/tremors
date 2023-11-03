package toph.centre

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.Mockito.verify
import toph.TophSpec
import toph.model.TophUserFixture
import toph.repository.UserRepository
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object UserCentreSpec extends TophSpec:

  override def spec = suite("UserCentreSpec")(
    test(s"An UseCentre should invoke ${classOf[UserRepository]}.update.") {
      val expectedUser = TophUserFixture.createRandom()

      for
        _    <- SweetMockitoLayer[UserRepository]
                  .whenF2(_.update(expectedUser.id, UserRepository.Update("Albert")))
                  .thenReturn(expectedUser)
        user <- ZIO.serviceWithZIO[UserCentre](_.update(expectedUser.id, UserCentre.Update("Albert")))
      yield assertTrue(
        user == expectedUser
      )
    },
    test(s"Am UserCentre should invoke ${classOf[UserRepository]}.add.") {
      val expectedUser = TophUserFixture.createRandom()
      for
        _          <- SweetMockitoLayer[UserRepository].whenF2(_.add(expectedUser)).thenReturn(expectedUser)
        result     <- ZIO.serviceWithZIO[UserCentre](_.add(expectedUser))
        repository <- ZIO.service[UserRepository]
      yield assertTrue(
        result == expectedUser,
        verify(repository).add(expectedUser) == null
      )
    }
  ).provideSome(
    SweetMockitoLayer.newMockLayer[UserRepository],
    ZLayer {
      ZIO.serviceWith[UserRepository](UserCentre.apply)
    }
  )
