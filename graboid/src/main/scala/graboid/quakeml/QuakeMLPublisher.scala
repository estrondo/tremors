package graboid.quakeml

import graboid.Crawler
import quakeml.DetectedEvent
import quakeml.Event

import java.time.ZonedDateTime

private[quakeml] object QuakeMLPublisher:

  def apply(now: ZonedDateTime, element: Element): DetectedEvent =
    element.name match
      case "event" => DetectedEvent(now, ElementReader[Event].apply(element))
