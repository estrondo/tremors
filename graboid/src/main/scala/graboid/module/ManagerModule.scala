package graboid.module

import com.softwaremill.macwire.Module
import com.softwaremill.macwire.wireWith
import graboid.manager.FDSNDataCentreManager
import zio.Task
import zio.ZIO

@Module
trait ManagerModule:

  def fdsnDataCentreManager: FDSNDataCentreManager

object ManagerModule:

  def apply(repositoryModule: RepositoryModule): Task[ManagerModule] =
    ZIO.succeed(Impl(repositoryModule))

  class Impl(repositoryModule: RepositoryModule) extends ManagerModule:

    override val fdsnDataCentreManager: FDSNDataCentreManager = wireWith(FDSNDataCentreManager.apply)
