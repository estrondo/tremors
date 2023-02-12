package toph.module

import com.softwaremill.macwire.wire
import toph.manager.EventManager
import toph.manager.MagnitudeManager
import toph.manager.SpatialManager
import zio.Task
import zio.ZIO

trait CoreModule:

  val eventManager: EventManager

  val spatialManager: SpatialManager

  val magnitudeManager: MagnitudeManager

object CoreModule:

  def apply(repositoryModule: RepositoryModule): Task[CoreModule] =
    ZIO.succeed(wire[Impl])

  private class Impl(repositoryModule: RepositoryModule) extends CoreModule:

    override val spatialManager: SpatialManager = SpatialManager(
      epicentreRepository = repositoryModule.epicentreRepository,
      hypocentreRepository = repositoryModule.hypocentreRepository
    )

    override val magnitudeManager: MagnitudeManager = MagnitudeManager(repositoryModule.magnitudeRepository)

    override val eventManager: EventManager =
      EventManager(repositoryModule.eventRepository, spatialManager, magnitudeManager)
