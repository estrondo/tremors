package graboid.protocol

import io.bullet.borer.Codec
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveAllCodecs

sealed abstract class GraboidCommand:

  def commandId: String

sealed abstract class DataCentreCommand extends GraboidCommand

case class CreateDataCentre(commandId: String, id: String, url: String) extends DataCentreCommand

case class UpdateDataCentre(commandId: String, id: String, url: String) extends DataCentreCommand

case class DeleteDataCentre(commandId: String, id: String) extends DataCentreCommand


object GraboidCommand:
  given Codec[GraboidCommand] = deriveAllCodecs
