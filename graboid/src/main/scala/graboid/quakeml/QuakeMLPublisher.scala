package graboid.quakeml

import graboid.Crawler
import quakeml.QuakeMLDetectedEvent
import quakeml.QuakeMLEvent

import java.time.ZonedDateTime

private[quakeml] object QuakeMLPublisher:

  def apply(now: ZonedDateTime, element: Element): QuakeMLDetectedEvent =
    element.name match
      case "event" => QuakeMLDetectedEvent(now, ElementReader[QuakeMLEvent].apply(element))
