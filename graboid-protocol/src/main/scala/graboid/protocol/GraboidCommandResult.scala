package graboid.protocol

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.{deriveCodec, deriveAllCodecs}
import graboid.protocol.GraboidCommandResult.Status

object GraboidCommandResult:

  sealed trait Status

  case class Ok(message: String)        extends Status
  case class Failed(cause: Seq[String]) extends Status

  given Codec[Status]               = deriveAllCodecs
  given Codec[GraboidCommandResult] = deriveCodec

case class GraboidCommandResult(id: String, time: Long, status: Status)
