package tremors.quakeml

import java.net.URI

object Event:

  case class Type(value: String)

  case class TypeCertainty(value: String)

  case class DescriptionType(value: String)

  case class Description(
      text: String,
      `type`: DescriptionType
  )

  case class OriginRerefence(resourceID: URI)

  case class MagnitudeReference(resourceID: URI)

case class Event(
    publicID: ResourceReference,
    preferredOriginID: Option[ResourceReference],
    preferredMagnitudeID: Option[ResourceReference],
    `type`: Option[Event.Type],
    typeCertainty: Option[Event.TypeCertainty],
    description: Seq[Event.Description],
    comment: Seq[Comment],
    creationInfo: Option[CreationInfo],
    magnitude: Seq[Magnitude]
)
