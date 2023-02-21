package toph.model.data

case class EventData(
    key: String,
    preferredOriginKey: Option[String],
    preferedMagnitudeKey: Option[String],
    `type`: Option[String],
    typeUncertainty: Option[String],
    description: Seq[String],
    comment: Seq[String],
    creationInfo: Option[CreationInfoData],
    originKey: Seq[String],
    magnitudeKey: Seq[String]
)
