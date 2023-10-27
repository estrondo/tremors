package toph.module

import one.estrondo.farango.IndexDescription
import toph.repository.UserRepository
import tremors.zio.farango.FarangoModule
import zio.Task

class RepositoryModule(
    val userRepository: UserRepository
)

object RepositoryModule:

  def apply(farangoModule: FarangoModule): Task[RepositoryModule] =
    for userRepository <- farangoModule
                            .collection(
                              "user",
                              Seq(
                                IndexDescription.Persistent(Seq("email"))
                              )
                            )
                            .flatMap(UserRepository.apply)
    yield new RepositoryModule(userRepository)
