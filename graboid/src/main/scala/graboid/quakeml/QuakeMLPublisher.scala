package graboid.quakeml

import graboid.Crawler
import quakeml.Event

private[quakeml] object QuakeMLPublisher:

  def apply(element: Element): Crawler.Info =
    element.name match
      case "event" => ElementReader[Event].apply(element)
