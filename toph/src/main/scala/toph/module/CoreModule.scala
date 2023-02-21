package toph.module

import com.softwaremill.macwire.wire
import toph.manager.EventDataManager
import toph.manager.MagnitudeDataManager
import toph.manager.SpatialManager
import zio.Task
import zio.ZIO
import core.KeyGenerator

trait CoreModule:

  val eventManager: EventDataManager

  val spatialManager: SpatialManager

  val magnitudeManager: MagnitudeDataManager

object CoreModule:

  def apply(repositoryModule: RepositoryModule): Task[CoreModule] =
    ZIO.succeed(wire[Impl])

  private class Impl(repositoryModule: RepositoryModule) extends CoreModule:

    override val spatialManager: SpatialManager =
      SpatialManager(
        hypocentreRepository = repositoryModule.hypocentreRepository,
        eventRepository = repositoryModule.queriableEventRepository,
        keyGenerator = KeyGenerator
      )

    override val magnitudeManager: MagnitudeDataManager = MagnitudeDataManager(repositoryModule.magnitudeRepository)

    override val eventManager: EventDataManager =
      EventDataManager(repositoryModule.eventRepository, spatialManager, magnitudeManager)
