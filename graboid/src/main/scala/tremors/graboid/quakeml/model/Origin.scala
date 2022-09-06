package tremors.graboid.quakeml.model

object Origin:

  case class DepthType(value: String)

  case class Type(value: String)

case class Origin(
    publicID: ResourceReference,
    time: TimeQuantity,
    longitude: RealQuantity,
    latitude: RealQuantity,
    depth: Option[RealQuantity],
    depthType: Option[Origin.DepthType],
    referenceSystemID: Option[ResourceReference],
    methodID: Option[ResourceReference],
    earthModelID: Option[ResourceReference],
    `type`: Option[Origin.Type],
    region: Option[String],
    evaluationMode: Option[EvaluationMode],
    evaluationStatus: Option[EvaluationStatus],
    comment: Seq[Comment],
    creationInfo: Option[CreationInfo]
)
