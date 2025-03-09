package toph.module

import toph.TimeService
import toph.service.AccountService
import toph.service.EventService
import toph.service.ObjectStorageReadService
import toph.service.ObjectStorageService
import toph.service.ObjectStorageUpdateService
import tremors.generator.KeyGenerator
import zio.Task
import zio.ZIO

class CentreModule(
    val eventCentre: EventService,
    val accountService: AccountService,
    val systemUserReadStorageService: ObjectStorageReadService,
    val systemUserUpdateStorageService: ObjectStorageUpdateService,
    val userReadStorageService: ObjectStorageReadService,
    val userUpdateStorageService: ObjectStorageUpdateService,
)

object CentreModule:

  def apply(repository: RepositoryModule): Task[CentreModule] =
    ZIO.attempt {
      val fixedSystemGetContext = ObjectStorageService.FixedOwnerContext("system")
      val userGetContext        = ObjectStorageService.UserContext()
      new CentreModule(
        eventCentre = EventService(repository.eventRepository),
        accountService = AccountService(repository.userRepository, KeyGenerator),
        systemUserReadStorageService = ObjectStorageReadService(
          repository = repository.systemObjectStorageRepository,
          getContext = fixedSystemGetContext,
        ),
        systemUserUpdateStorageService = ObjectStorageUpdateService(
          repository = repository.systemObjectStorageRepository,
          getContext = fixedSystemGetContext,
          keyGenerator = KeyGenerator,
          timeService = TimeService,
        ),
        userReadStorageService = ObjectStorageReadService(
          repository = repository.userObjectStorageRepository,
          getContext = userGetContext,
        ),
        userUpdateStorageService = ObjectStorageUpdateService(
          repository = repository.userObjectStorageRepository,
          getContext = userGetContext,
          keyGenerator = KeyGenerator,
          timeService = TimeService,
        ),
      )
    }
