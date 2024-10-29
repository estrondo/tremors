package toph.module

import toph.service.AccountService
import toph.service.EventService
import zio.Task
import zio.ZIO

class CentreModule(
    val eventCentre: EventService,
    val accountService: AccountService,
)

object CentreModule:

  def apply(repository: RepositoryModule): Task[CentreModule] =
    ZIO.attempt(
      new CentreModule(
        eventCentre = EventService(repository.eventRepository),
        accountService = AccountService(repository.userRepository),
      ),
    )
