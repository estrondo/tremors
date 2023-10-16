package graboid.quakeml.reader

import graboid.GraboidException
import graboid.quakeml.parser.Element

trait AttributeReader[+T]:

  def apply(name: String, element: Element): T

object AttributeReader:

  given [T: TextReader]: AttributeReader[Option[T]] with
    override def apply(name: String, element: Element): Option[T] =
      element.attributes.get(name) match
        case Some(value) => Some(summon[TextReader[T]](value))
        case None        => None

  given [T: TextReader]: AttributeReader[T] with

    override def apply(name: String, element: Element): T =
      element.attributes.get(name) match
        case Some(value) => summon[TextReader[T]](value)
        case None        => throw GraboidException.QuakeML(s"Element with no attribute $name!")
