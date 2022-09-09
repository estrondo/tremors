package tremors.graboid.quakeml.model

case class Comment(
    text: String,
    id: Option[ResourceReference],
    creationInfo: Option[CreationInfo]
)
