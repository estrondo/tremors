package graboid.quakeml

import java.time.ZonedDateTime
import quakeml.QuakeMLDetectedEvent
import quakeml.QuakeMLEvent

private[quakeml] object QuakeMLPublisher:

  def apply(now: ZonedDateTime, element: Element): QuakeMLDetectedEvent =
    element.name match
      case "event" => QuakeMLDetectedEvent(now, ElementReader[QuakeMLEvent].apply(element))
