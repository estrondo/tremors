package graboid.protocol

sealed abstract class GraboidCommand:

  def commandId: String

sealed abstract class DataCentreCommand extends GraboidCommand

case class CreateDataCentre(commandId: String, id: String, url: String) extends DataCentreCommand

case class UpdateDataCentre(commandId: String, id: String, url: String) extends DataCentreCommand

case class DeleteDataCentre(commandId: String, id: String) extends DataCentreCommand
