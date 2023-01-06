package graboid

import _root_.quakeml.Event
import _root_.quakeml.Magnitude
import _root_.quakeml.Origin
import zio.Task
import zio.stream.ZStream

import java.time.ZonedDateTime
import _root_.quakeml.CreationInfo
import io.github.arainko.ducktape.Transformer

object Crawler:

  type Info   = Event | Origin | Magnitude
  type Stream = ZStream[Any, Throwable, Crawler.Info]

  enum Type:
    case Undefined
    case FDSN
    case Other

trait Crawler:

  def crawl(timeWindow: TimeWindow): Task[Crawler.Stream]
