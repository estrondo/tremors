package tremors.graboid.quakeml

import tremors.quakeml.ResourceReference
import tremors.quakeml.Event
import tremors.quakeml.Comment
import tremors.quakeml.CreationInfo
import java.time.ZonedDateTime
import tremors.quakeml.Magnitude
import tremors.quakeml.RealQuantity
import tremors.quakeml.EvaluationMode
import tremors.quakeml.EvaluationStatus

private[quakeml] object ElementReader:

  given ElementReader[String]                = element => element.text
  given ElementReader[ResourceReference]     = element => ResourceReference(element.text)
  given ElementReader[Event.Type]            = element => Event.Type(element.text)
  given ElementReader[Event.TypeCertainty]   = element => Event.TypeCertainty(element.text)
  given ElementReader[Event.DescriptionType] = element => Event.DescriptionType(element.text)
  given ElementReader[ZonedDateTime]         = element => ZonedDateTime.parse(element.text)
  given ElementReader[Int]                   = element => element.text.toInt
  given ElementReader[Double]                = element => element.text.toDouble
  given ElementReader[EvaluationMode]        = element => EvaluationMode(element.text)
  given ElementReader[EvaluationStatus]      = element => EvaluationStatus(element.text)

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

  given ElementReader[RealQuantity] = element =>
    RealQuantity(
      value = ChildReader.read("value", element),
      uncertainty = ChildReader.read("uncertainty", element)
    )

private[quakeml] trait ElementReader[T] extends (Element => T)
