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

  private val EventTypeName     = classOf[Event].getSimpleName()
  private val OriginTypeName    = classOf[Origin].getSimpleName()
  private val MagnitudeTypeName = classOf[Magnitude].getSimpleName()

  given encoder: Encoder[Info] with

    override def write(w: Writer, value: Info): Writer =
      value match
        case event: Event         =>
          Encoder[Event].write(w.writeString(EventTypeName), event)
        case origin: Origin       =>
          Encoder[Origin].write(w.writeString(OriginTypeName), origin)
        case magnitude: Magnitude =>
          Encoder[Magnitude].write(w.writeString(MagnitudeTypeName), magnitude)

  given decoder: Decoder[Info] with
    override def read(r: Reader): Info =
      r.readString() match
        case EventTypeName     => Decoder[Event].read(r)
        case OriginTypeName    => Decoder[Origin].read(r)
        case MagnitudeTypeName => Decoder[Magnitude].read(r)
        case value             => throw IllegalArgumentException(s"Invalid Info Number: $value")

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
