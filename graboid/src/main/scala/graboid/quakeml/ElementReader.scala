package graboid.quakeml

import quakeml.ResourceReference
import quakeml.Event
import quakeml.Comment
import quakeml.CreationInfo
import java.time.ZonedDateTime
import quakeml.Magnitude
import quakeml.RealQuantity
import quakeml.EvaluationMode
import quakeml.EvaluationStatus
import quakeml.Origin
import quakeml.TimeQuantity

private[quakeml] object ElementReader:

  given ElementReader[String]                  = element => element.text
  given ElementReader[ResourceReference]       = element => ResourceReference(element.text)
  given eventType: ElementReader[Event.Type]   = element => Event.Type(element.text)
  given ElementReader[Event.TypeCertainty]     = element => Event.TypeCertainty(element.text)
  given ElementReader[Event.DescriptionType]   = element => Event.DescriptionType(element.text)
  given ElementReader[ZonedDateTime]           = element => ZonedDateTime.parse(element.text)
  given ElementReader[Int]                     = element => element.text.toInt
  given ElementReader[Double]                  = element => element.text.toDouble
  given ElementReader[EvaluationMode]          = element => EvaluationMode(element.text)
  given ElementReader[EvaluationStatus]        = element => EvaluationStatus(element.text)
  given ElementReader[Origin.DepthType]        = element => Origin.DepthType(element.text)
  given originType: ElementReader[Origin.Type] = element => Origin.Type(element.text)

  def apply[T: ElementReader]: ElementReader[T] = summon[ElementReader[T]]

  given ElementReader[Comment] = element =>
    Comment(
      text = ChildReader.read("text", element),
      id = ChildReader.read("text", element),
      creationInfo = ChildReader.read("creationInfo", element)
    )

  given ElementReader[Event.Description] = element =>
    Event.Description(
      text = ChildReader.read("text", element),
      `type` = ChildReader.read("type", element)
    )

  given ElementReader[CreationInfo] = element =>
    CreationInfo(
      agencyID = ChildReader.read("agencyID", element),
      agencyURI = ChildReader.read("agencyURI", element),
      author = ChildReader.read("author", element),
      authorURI = ChildReader.read("authorURI", element),
      creationTime = ChildReader.read("creationTime", element),
      version = ChildReader.read("version", element)
    )

  given ElementReader[TimeQuantity] = element =>
    TimeQuantity(
      value = ChildReader.read("value", element),
      uncertainty = ChildReader.read("uncertainty", element)
    )

  given ElementReader[Origin] = element =>
    Origin(
      publicID = AttributeReader.read("publicID", element),
      time = ChildReader.read("time", element),
      longitude = ChildReader.read("longitude", element),
      latitude = ChildReader.read("latitude", element),
      depth = ChildReader.read("depth", element),
      depthType = ChildReader.read("depthType", element),
      referenceSystemID = ChildReader.read("referenceSystemID", element),
      methodID = ChildReader.read("methodID", element),
      earthModelID = ChildReader.read("earthModelID", element),
      `type` = ChildReader.read("type", element),
      region = ChildReader.read("region", element),
      evaluationMode = ChildReader.read("evaluationMode", element),
      evaluationStatus = ChildReader.read("evaluationStatus", element),
      comment = ChildReader.read("comment", element),
      creationInfo = ChildReader.read("creationInfo", element)
    )

  given ElementReader[Magnitude] = element =>
    Magnitude(
      publicID = AttributeReader.read("publicID", element),
      mag = ChildReader.read("mag", element),
      `type` = ChildReader.read("type", element),
      originID = ChildReader.read("originID", element),
      methodID = ChildReader.read("methodID", element),
      stationCount = ChildReader.read("stationCount", element),
      azimuthalGap = ChildReader.read("azimuthalGap", element),
      evaluationMode = ChildReader.read("evaluationMode", element),
      evaluationStatus = ChildReader.read("evaluationStatus", element),
      comment = ChildReader.read("comment", element),
      creationInfo = ChildReader.read("creationInfo", element)
    )

  given ElementReader[Event] = element =>
    Event(
      publicID = AttributeReader.read("publicID", element),
      preferredOriginID = ChildReader.read("preferredOriginID", element),
      preferredMagnitudeID = ChildReader.read("preferredMagnitudeID", element),
      `type` = ChildReader.read("type", element),
      typeCertainty = ChildReader.read("typeCertainty", element),
      description = ChildReader.read("description", element),
      comment = ChildReader.read("comment", element),
      creationInfo = ChildReader.read("creationInfo", element),
      origin = ChildReader.read("origin", element),
      magnitude = ChildReader.read("magnitude", element)
    )

  given ElementReader[RealQuantity] = element =>
    RealQuantity(
      value = ChildReader.read("value", element),
      uncertainty = ChildReader.read("uncertainty", element)
    )

private[quakeml] trait ElementReader[T] extends (Element => T)
