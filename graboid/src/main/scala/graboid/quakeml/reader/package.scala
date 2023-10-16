package graboid.quakeml.reader

import graboid.GraboidException
import graboid.quakeml.parser.Element

def readAttribute[T: AttributeReader](name: String, element: Element): T =
  try summon[AttributeReader[T]](name, element)
  catch
    case cause: Throwable =>
      throw GraboidException.QuakeML(s"An error occurred during reading of attribute $name!", cause)

def readElement[T: ElementReader](element: Element): T =
  summon[ElementReader[T]](element)

def readChild[T: ChildElementReader](name: String, element: Element): T =
  try summon[ChildElementReader[T]](name, element)
  catch
    case cause: Throwable =>
      throw GraboidException.QuakeML(s"An error occurred during reading of child element $name!", cause)
