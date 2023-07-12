package graboid.protocol

sealed abstract class GraboidCommandResult

case class GraboidCommandSuccess(id: String) extends GraboidCommandResult

case class GraboidCommandFailure(id: String, message: String) extends GraboidCommandResult
