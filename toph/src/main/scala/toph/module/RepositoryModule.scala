package toph.module

import one.estrondo.farango.IndexDescription
import toph.repository.EventRepository
import toph.repository.UserRepository
import tremors.zio.farango.FarangoModule
import zio.Task

class RepositoryModule(
    val eventRepository: EventRepository,
    val userRepository: UserRepository
)

object RepositoryModule:

  def apply(farangoModule: FarangoModule): Task[RepositoryModule] =
    for
      eventRepository <- farangoModule
                           .collection(
                             "event"
                           )
                           .flatMap(EventRepository.apply)
      userRepository  <- farangoModule
                           .collection(
                             "user",
                             Seq(
                               IndexDescription.Persistent(Seq("email"))
                             )
                           )
                           .flatMap(UserRepository.apply)
    yield new RepositoryModule(
      eventRepository,
      userRepository
    )
