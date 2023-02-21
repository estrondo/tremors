package quakeml

case class QuakeMLComment(
    text: String,
    id: Option[QuakeMLResourceReference],
    creationInfo: Option[QuakeMLCreationInfo]
)
