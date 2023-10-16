package graboid.quakeml.reader

import graboid.GraboidException
import graboid.quakeml.parser.Element

trait ChildElementReader[+T]:

  def apply(name: String, element: Element): T

object ChildElementReader:

  given [T: ElementReader]: ChildElementReader[Option[T]] with

    override def apply(name: String, element: Element): Option[T] =
      element.children.collectFirst {
        case child if child.name == name => summon[ElementReader[T]](child)
      }

  given [T: ElementReader]: ChildElementReader[Seq[T]] with

    override def apply(name: String, element: Element): Seq[T] =
      element.children.collect {
        case child if child.name == name => summon[ElementReader[T]](child)
      }

  given [T: ElementReader]: ChildElementReader[T] with

    override def apply(name: String, element: Element): T =
      element.children.find(_.name == name) match
        case Some(child) => summon[ElementReader[T]](child)
        case None        => throw GraboidException.QuakeML(s"There is no child element $name!")
