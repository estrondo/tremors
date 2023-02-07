package toph.model

case class Event(
    key: String,
    preferredOriginKey: Option[String],
    preferedMagnitudeKey: Option[String],
    `type`: Option[String],
    typeUncertainty: Option[String],
    description: Seq[String],
    comment: Seq[String],
    creationInfo: Option[CreationInfo],
    originKey: Seq[String],
    magnitudeKey: Seq[String]
)
