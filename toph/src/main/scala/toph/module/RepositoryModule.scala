package toph.module

import one.estrondo.farango.IndexDescription
import toph.repository.AccountRepository
import toph.repository.EventRepository
import tremors.zio.farango.FarangoModule
import zio.Task

class RepositoryModule(
    val eventRepository: EventRepository,
    val userRepository: AccountRepository,
)

object RepositoryModule:

  def apply(farangoModule: FarangoModule): Task[RepositoryModule] =
    for
      eventRepository <- farangoModule
                           .collection(
                             "event",
                           )
                           .flatMap(EventRepository.apply)
      userRepository  <- farangoModule
                           .collection(
                             "user",
                             Seq(
                               IndexDescription.Persistent(Seq("email")),
                             ),
                           )
                           .flatMap(AccountRepository.apply)
    yield new RepositoryModule(
      eventRepository,
      userRepository,
    )
