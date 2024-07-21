package toph.module

import toph.centre.EventCentre
import toph.centre.AccountService
import zio.Task
import zio.ZIO

class CentreModule(
    val eventCentre: EventCentre,
    val userCentre: AccountService,
)

object CentreModule:

  def apply(repository: RepositoryModule): Task[CentreModule] =
    ZIO.attempt(
      new CentreModule(
        eventCentre = EventCentre(repository.eventRepository),
        userCentre = AccountService(repository.userRepository),
      ),
    )
