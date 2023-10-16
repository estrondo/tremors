package graboid.module

import graboid.manager.DataCentreManager
import zio.Task
import zio.ZIO

trait ManagerModule:

  def dataCentreManager: DataCentreManager

object ManagerModule:

  def apply(repositoryModule: RepositoryModule): Task[ManagerModule] =
    ZIO.succeed(Impl(repositoryModule))

  class Impl(repositoryModule: RepositoryModule) extends ManagerModule:

    override val dataCentreManager: DataCentreManager = DataCentreManager(repositoryModule.dataCentreRepository)
