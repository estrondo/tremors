package graboid.quakeml.reader

import graboid.quakeml.parser.Element
import tremors.quakeml.Event
import zio.Task
import zio.ZIO

object QuakeMLReader:

  def apply(element: Element): Task[Event] = ZIO.attempt {
    readElement[Event](element)
  }
