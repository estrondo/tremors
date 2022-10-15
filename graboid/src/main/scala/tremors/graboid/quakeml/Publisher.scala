package tremors.graboid.quakeml

import tremors.graboid.Crawler
import tremors.quakeml.Event

private[quakeml] object Publisher:

  def apply(element: Element): Crawler.Info =
    element.name match
      case "event" => publishEvent(element)

  def publishEvent(element: Element) = Event(
    publicID = AttributeReader.read("publicID", element),
    preferredOriginID = ChildReader.read("preferredOriginID", element),
    preferredMagnitudeID = ChildReader.read("preferredMagnitudeID", element),
    `type` = ChildReader.read("type", element),
    typeCertainty = ChildReader.read("typeCertainty", element),
    description = ChildReader.read("description", element),
    comment = ChildReader.read("comment", element),
    creationInfo = ChildReader.read("creationInfo", element),
    magnitude = ChildReader.read("magnitude", element)
  )
