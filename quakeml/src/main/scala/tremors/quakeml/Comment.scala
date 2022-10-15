package tremors.quakeml

case class Comment(
    text: String,
    id: Option[ResourceReference],
    creationInfo: Option[CreationInfo]
)
