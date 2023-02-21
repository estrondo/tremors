package quakeml

case class QuakeMLMagnitude(
    publicID: QuakeMLResourceReference,
    mag: QuakeMLRealQuantity,
    `type`: Option[String],
    originID: Option[QuakeMLResourceReference],
    methodID: Option[QuakeMLResourceReference],
    stationCount: Option[Int],
    azimuthalGap: Option[Double],
    evaluationMode: Option[QuakeMLEvaluationMode],
    evaluationStatus: Option[QuakeMLEvaluationStatus],
    comment: Seq[QuakeMLComment],
    creationInfo: Option[QuakeMLCreationInfo]
)
