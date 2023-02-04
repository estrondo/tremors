package graboid.protocol

import graboid.protocol.GraboidCommandResult.Status
import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveAllCodecs
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec
import scala.annotation.tailrec

object GraboidCommandResult:

  sealed trait Status

  case class Ok(message: String)        extends Status
  case class Failed(cause: Seq[String]) extends Status

  given Codec[Status]               = deriveAllCodecs
  given Codec[GraboidCommandResult] = deriveCodec

  def failed(message: String, cause: Throwable): Failed =

    @tailrec def toSeq(throwable: Throwable, result: Seq[String]): Seq[String] =
      if throwable != null then toSeq(throwable.getCause(), result :+ throwable.getMessage())
      else result

    Failed(message +: toSeq(cause, Vector.empty))

case class GraboidCommandResult(id: String, time: Long, status: Status)
