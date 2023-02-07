package graboid

import _root_.quakeml.CreationInfo
import _root_.quakeml.DetectedEvent
import _root_.quakeml.Event
import _root_.quakeml.Magnitude
import _root_.quakeml.Origin
import cbor.quakeml.given
import io.bullet.borer.Codec
import io.bullet.borer.Decoder
import io.bullet.borer.Encoder
import io.bullet.borer.Reader
import io.bullet.borer.Writer
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec
import io.github.arainko.ducktape.Transformer
import zio.Task
import zio.stream.ZStream

import java.time.ZonedDateTime

object Crawler:

  enum Type:
    case FDSN

trait Crawler:

  def crawl(timeWindow: TimeWindow): Task[ZStream[Any, Throwable, DetectedEvent]]
