package graboid.quakeml

import quakeml.QuakeMLComment
import quakeml.QuakeMLCreationInfo
import quakeml.QuakeMLEvaluationMode
import quakeml.QuakeMLEvaluationStatus
import quakeml.QuakeMLEvent
import quakeml.QuakeMLMagnitude
import quakeml.QuakeMLOrigin
import quakeml.QuakeMLRealQuantity
import quakeml.QuakeMLResourceReference
import quakeml.QuakeMLTimeQuantity

import java.time.ZonedDateTime

private[quakeml] object ElementReader:

  given ElementReader[String]                         = element => element.text
  given ElementReader[QuakeMLResourceReference]       = element => QuakeMLResourceReference(element.text)
  given eventType: ElementReader[QuakeMLEvent.Type]   = element => QuakeMLEvent.Type(element.text)
  given ElementReader[QuakeMLEvent.TypeCertainty]     = element => QuakeMLEvent.TypeCertainty(element.text)
  given ElementReader[QuakeMLEvent.DescriptionType]   = element => QuakeMLEvent.DescriptionType(element.text)
  given ElementReader[ZonedDateTime]                  = element => ZonedDateTime.parse(element.text)
  given ElementReader[Int]                            = element => element.text.toInt
  given ElementReader[Double]                         = element => element.text.toDouble
  given ElementReader[QuakeMLEvaluationMode]          = element => QuakeMLEvaluationMode(element.text)
  given ElementReader[QuakeMLEvaluationStatus]        = element => QuakeMLEvaluationStatus(element.text)
  given ElementReader[QuakeMLOrigin.DepthType]        = element => QuakeMLOrigin.DepthType(element.text)
  given originType: ElementReader[QuakeMLOrigin.Type] = element => QuakeMLOrigin.Type(element.text)

  def apply[T: ElementReader]: ElementReader[T] = summon[ElementReader[T]]

  given ElementReader[QuakeMLComment] = element =>
    QuakeMLComment(
      text = ChildReader.read("text", element),
      id = ChildReader.read("text", element),
      creationInfo = ChildReader.read("creationInfo", element)
    )

  given ElementReader[QuakeMLEvent.Description] = element =>
    QuakeMLEvent.Description(
      text = ChildReader.read("text", element),
      `type` = ChildReader.read("type", element)
    )

  given ElementReader[QuakeMLCreationInfo] = element =>
    QuakeMLCreationInfo(
      agencyID = ChildReader.read("agencyID", element),
      agencyURI = ChildReader.read("agencyURI", element),
      author = ChildReader.read("author", element),
      authorURI = ChildReader.read("authorURI", element),
      creationTime = ChildReader.read("creationTime", element),
      version = ChildReader.read("version", element)
    )

  given ElementReader[QuakeMLTimeQuantity] = element =>
    QuakeMLTimeQuantity(
      value = ChildReader.read("value", element),
      uncertainty = ChildReader.read("uncertainty", element)
    )

  given ElementReader[QuakeMLOrigin] = element =>
    QuakeMLOrigin(
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

  given ElementReader[QuakeMLMagnitude] = element =>
    QuakeMLMagnitude(
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

  given ElementReader[QuakeMLEvent] = element =>
    QuakeMLEvent(
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

  given ElementReader[QuakeMLRealQuantity] = element =>
    QuakeMLRealQuantity(
      value = ChildReader.read("value", element),
      uncertainty = ChildReader.read("uncertainty", element)
    )

private[quakeml] trait ElementReader[T] extends (Element => T)
