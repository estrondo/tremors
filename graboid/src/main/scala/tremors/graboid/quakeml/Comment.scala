package tremors.graboid.quakeml

case class Comment(
    text: String,
    id: Option[ResourceReference],
    creationInfo: Option[CreationInfo]
)
