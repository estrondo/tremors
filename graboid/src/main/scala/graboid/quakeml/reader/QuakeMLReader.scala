package graboid.quakeml.reader

import graboid.quakeml.Event
import graboid.quakeml.parser.Element
import zio.Task
import zio.ZIO

object QuakeMLReader:

  def apply(element: Element): Task[Event] = ZIO.attempt {
    readElement[Event](element)
  }
