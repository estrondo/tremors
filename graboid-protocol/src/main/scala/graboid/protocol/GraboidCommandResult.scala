package graboid.protocol

import graboid.protocol.GraboidCommandResult.Status
import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveAllCodecs
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec
import scala.annotation.tailrec

object GraboidCommandResult:

  sealed trait Status

  case class Ok(message: String, attributes: Map[String, String])                         extends Status
  case class Failed(message: String, cause: Seq[String], attributes: Map[String, String]) extends Status

  given Codec[Status]               = deriveAllCodecs
  given Codec[GraboidCommandResult] = deriveCodec

  def ok(message: String, attributes: (String, String)*): Ok =
    Ok(message, Map(attributes*))

  def failed(message: String, cause: Seq[String] = Nil, attributes: (String, String)*): Failed =
    Failed(message, cause, Map(attributes*))

  def failed(message: String, cause: Throwable, attributes: (String, String)*): Failed =

    @tailrec def toSeq(throwable: Throwable, result: Seq[String]): Seq[String] =
      if throwable != null then toSeq(throwable.getCause(), result :+ throwable.getMessage())
      else result

    Failed(message, toSeq(cause, Vector.empty), Map(attributes*))

case class GraboidCommandResult(id: String, time: Long, status: Status)
