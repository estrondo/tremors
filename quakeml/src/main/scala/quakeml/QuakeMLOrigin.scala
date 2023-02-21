package quakeml

object QuakeMLOrigin:

  case class DepthType(value: String)

  case class Type(value: String)

case class QuakeMLOrigin(
    publicID: QuakeMLResourceReference,
    time: QuakeMLTimeQuantity,
    longitude: QuakeMLRealQuantity,
    latitude: QuakeMLRealQuantity,
    depth: Option[QuakeMLRealQuantity],
    depthType: Option[QuakeMLOrigin.DepthType],
    referenceSystemID: Option[QuakeMLResourceReference],
    methodID: Option[QuakeMLResourceReference],
    earthModelID: Option[QuakeMLResourceReference],
    `type`: Option[QuakeMLOrigin.Type],
    region: Option[String],
    evaluationMode: Option[QuakeMLEvaluationMode],
    evaluationStatus: Option[QuakeMLEvaluationStatus],
    comment: Seq[QuakeMLComment],
    creationInfo: Option[QuakeMLCreationInfo]
)
