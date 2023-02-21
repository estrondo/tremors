package quakeml

import java.net.URI

object QuakeMLEvent:

  case class Type(value: String)

  case class TypeCertainty(value: String)

  case class DescriptionType(value: String)

  case class Description(
      text: String,
      `type`: DescriptionType
  )

  case class OriginRerefence(resourceID: URI)

  case class MagnitudeReference(resourceID: URI)

case class QuakeMLEvent(
    publicID: QuakeMLResourceReference,
    preferredOriginID: Option[QuakeMLResourceReference],
    preferredMagnitudeID: Option[QuakeMLResourceReference],
    `type`: Option[QuakeMLEvent.Type],
    typeCertainty: Option[QuakeMLEvent.TypeCertainty],
    description: Seq[QuakeMLEvent.Description],
    comment: Seq[QuakeMLComment],
    creationInfo: Option[QuakeMLCreationInfo],
    origin: Seq[QuakeMLOrigin],
    magnitude: Seq[QuakeMLMagnitude]
)
