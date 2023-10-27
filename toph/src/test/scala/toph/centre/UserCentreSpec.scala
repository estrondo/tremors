package toph.centre

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import toph.TophSpec
import toph.model.TophUserFixture
import toph.repository.UserRepository
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object UserCentreSpec extends TophSpec:

  override def spec = suite("UserCentreSpec")(
    test(s"An UseCentre should invoke ${classOf[UserRepository]}.update") {
      val expectedUser = TophUserFixture.createRandom()

      for
        _    <- SweetMockitoLayer[UserRepository]
                  .whenF2(_.update(expectedUser.id, UserRepository.Update("Albert")))
                  .thenReturn(expectedUser)
        user <- ZIO.serviceWithZIO[UserCentre](_.update(expectedUser.id, UserCentre.Update("Albert")))
      yield assertTrue(
        user == expectedUser
      )
    }
  ).provideSome(
    SweetMockitoLayer.newMockLayer[UserRepository],
    ZLayer {
      ZIO.serviceWith[UserRepository](UserCentre.apply)
    }
  )
