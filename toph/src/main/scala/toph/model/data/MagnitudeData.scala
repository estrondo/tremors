package toph.model.data

case class MagnitudeData(
    key: String,
    mag: Double,
    `type`: Option[String],
    originID: Option[String],
    methodID: Option[String],
    stationCount: Option[Int],
    azimuthalGap: Option[Double],
    evaluationMode: Option[String],
    evaluationStatus: Option[String],
    comment: Seq[String],
    creationInfo: Option[CreationInfoData]
)
