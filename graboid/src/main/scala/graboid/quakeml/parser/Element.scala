package graboid.quakeml.parser

case class Element(
    name: String,
    attributes: Map[String, String],
    content: Option[String],
    children: Seq[Element]
)
