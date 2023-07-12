package graboid.protocol

sealed abstract class GraboidCommand:

  def commandId: String

case class CreateDataCentre(commandId: String, id: String, url: String) extends GraboidCommand

case class UpdateDataCentre(commandId: String, id: String, url: String) extends GraboidCommand

case class DeleteDataCentre(commandId: String, id: String) extends GraboidCommand
