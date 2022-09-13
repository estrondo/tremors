package tremors.graboid.quakeml

import tremors.graboid.quakeml.model.ResourceReference

private[quakeml] object AttributeReader:

  given AttributeReader[ResourceReference] = (element, name) =>
    ResourceReference(element.attributes(name))

  def read[T: AttributeReader](attributeName: String, element: Element): T =
    try summon[AttributeReader[T]](element, attributeName)
    catch
      case cause =>
        throw IllegalArgumentException(
          s"It's impossible to read attribute $attributeName of ${element.name}!",
          cause
        )

private[quakeml] trait AttributeReader[T] extends ((Element, String) => T)
