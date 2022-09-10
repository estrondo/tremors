package tremors.graboid.quakeml

import tremors.graboid.Crawler
import tremors.graboid.quakeml.model.Event
import scala.collection.immutable.HashMap

private[quakeml] case class Element(
    name: String,
    attributes: HashMap[String, String],
    texts: Vector[String] = Vector.empty,
    childreen: Vector[Element] = Vector.empty
):

  def appendChild(newChild: Element): Element =
    copy(childreen = childreen :+ newChild)

  def appendText(newText: String): Element =
    copy(texts = this.texts :+ newText)

  def text: String = texts.mkString

  def getChild(name: String): Option[Element] =
    childreen.find(_.name == name)

  def child(childName: String): Element =
    getChild(childName) match
      case Some(child) => child
      case None        => throw IllegalStateException(s"There is no child $childName in $name!")

  def childreenOf(name: String): Vector[Element] =
    childreen.filter(_.name == name)
