package quakeml

case class Magnitude(
    publicID: ResourceReference,
    mag: RealQuantity,
    `type`: Option[String],
    originID: Option[ResourceReference],
    methodID: Option[ResourceReference],
    stationCount: Option[Int],
    azimuthalGap: Option[Double],
    evaluationMode: Option[EvaluationMode],
    evaluationStatus: Option[EvaluationStatus],
    comment: Seq[Comment],
    creationInfo: Option[CreationInfo]
)
