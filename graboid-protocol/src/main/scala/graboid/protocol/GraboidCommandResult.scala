package graboid.protocol

import io.bullet.borer.Codec
import io.bullet.borer.Encoder
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveAllCodecs

sealed abstract class GraboidCommandResult

case class GraboidCommandSuccess(id: String) extends GraboidCommandResult

case class GraboidCommandFailure(id: String, message: String) extends GraboidCommandResult

object GraboidCommandResult:

  given Codec[GraboidCommandResult] = deriveAllCodecs
