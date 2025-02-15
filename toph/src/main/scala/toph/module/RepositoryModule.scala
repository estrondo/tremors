package toph.module

import com.softwaremill.macwire.wire
import one.estrondo.farango.IndexDescription
import toph.repository.AccountRepository
import toph.repository.EventRepository
import toph.repository.TokenRepository
import tremors.zio.farango.FarangoModule
import zio.Task

class RepositoryModule(
    val eventRepository: EventRepository,
    val userRepository: AccountRepository,
    val tokenRepository: TokenRepository,
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
      tokenRepository <- farangoModule
                           .collection(
                             name = "token",
                           )
                           .map(TokenRepository.apply)
    yield wire[RepositoryModule]
