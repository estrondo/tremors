package graboid.command

import com.softwaremill.macwire.wire
import graboid.FDSNDataCentre
import graboid.GraboidException
import graboid.manager.FDSNDataCentreManager
import graboid.protocol.CreateDataCentre
import graboid.protocol.DataCentreCommand
import graboid.protocol.DeleteDataCentre
import graboid.protocol.UpdateDataCentre
import zio.Task
import zio.ZIO

trait DataCentreExecutor: 

  def apply(command: DataCentreCommand): Task[DataCentreCommand]

object DataCentreExecutor:

  def apply(manager: FDSNDataCentreManager): Task[DataCentreExecutor] =
    ZIO.succeed(wire[Impl])

  private class Impl(manager: FDSNDataCentreManager) extends DataCentreExecutor:

    override def apply(command: DataCentreCommand): Task[DataCentreCommand] =
      command match
        case CreateDataCentre(_, id, url) =>
          for _ <- manager
                     .add(FDSNDataCentre(id, url))
                     .mapError(fancyException(s"It was impossible to create the Data Centre $id!"))
          yield command
        case UpdateDataCentre(_, id, url) =>
          for _ <- manager
                     .update(FDSNDataCentre(id, url))
                     .mapError(fancyException(s"It was impossible to update the Data Centre $id!"))
          yield command
        case DeleteDataCentre(_, id)      =>
          for _ <- manager
                     .delete(id)
                     .mapError(fancyException(s"It was impossible to delete the Data Centre $id!"))
          yield command

    private def fancyException(message: => String)(cause: Throwable): GraboidException.Command =
      GraboidException.Command(message, cause)
