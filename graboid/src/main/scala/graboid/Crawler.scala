package graboid

import _root_.quakeml.Event
import _root_.quakeml.Magnitude
import _root_.quakeml.Origin
import zio.Task
import zio.stream.ZStream

import java.time.ZonedDateTime
import _root_.quakeml.CreationInfo
import io.github.arainko.ducktape.Transformer
import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec
import io.bullet.borer.Encoder
import io.bullet.borer.Writer
import cbor.quakeml.given
import io.bullet.borer.Decoder
import io.bullet.borer.Reader

object Crawler:

  type Info = Event | Origin | Magnitude

  given encoder: Encoder[Info] with

    override def write(w: Writer, value: Info): Writer =
      value match
        case event: Event         =>
          Encoder[Event].write(w.writeInt(0), event)
        case origin: Origin       =>
          Encoder[Origin].write(w.writeInt(1), origin)
        case magnitude: Magnitude =>
          Encoder[Magnitude].write(w.writeInt(2), magnitude)

  given decoder: Decoder[Info] with
    override def read(r: Reader): Info =
      r.readInt() match
        case 0     => Decoder[Event].read(r)
        case 1     => Decoder[Origin].read(r)
        case 2     => Decoder[Magnitude].read(r)
        case value => throw IllegalArgumentException(s"Invalid Info Number: $value")

  given Codec[Info] = Codec(encoder, decoder)

  enum Type:
    case FDSN

  def getID(info: Info): String =
    info match
      case e: Event     => e.publicID.uri
      case o: Origin    => o.publicID.uri
      case m: Magnitude => m.publicID.uri

trait Crawler:

  def crawl(timeWindow: TimeWindow): Task[ZStream[Any, Throwable, Crawler.Info]]
