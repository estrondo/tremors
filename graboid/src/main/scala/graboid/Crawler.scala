package graboid

import _root_.quakeml.QuakeMLDetectedEvent
import zio.Task
import zio.stream.ZStream

object Crawler:

  enum Type:
    case FDSN

trait Crawler:

  def crawl(timeWindow: TimeWindow): Task[ZStream[Any, Throwable, QuakeMLDetectedEvent]]
