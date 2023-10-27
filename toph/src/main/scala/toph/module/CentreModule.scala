package toph.module

import toph.centre.UserCentre
import zio.Task
import zio.ZIO

class CentreModule(
    val userCentre: UserCentre
)

object CentreModule:

  def apply(repository: RepositoryModule): Task[CentreModule] =
    ZIO.attempt(new CentreModule(UserCentre(repository.userRepository)))
