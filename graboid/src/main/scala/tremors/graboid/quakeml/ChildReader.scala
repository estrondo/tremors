package tremors.graboid.quakeml

import tremors.graboid.quakeml.model.ResourceReference
import tremors.graboid.quakeml.model.Event
import tremors.graboid.quakeml.model.Comment
import tremors.graboid.quakeml.model.CreationInfo
import java.time.ZonedDateTime
import tremors.graboid.quakeml.model.Magnitude
import tremors.graboid.quakeml.model.RealQuantity

private[quakeml] object ChildReader:

  given ChildReader[ResourceReference]     = newReader
  given ChildReader[Event.DescriptionType] = newReader
  given ChildReader[Event.Type]            = newReader
  given ChildReader[Event.TypeCertainty]   = newReader
  given ChildReader[Event.Description]     = newReader
  given ChildReader[Comment]               = newReader
  given ChildReader[CreationInfo]          = newReader
  given ChildReader[String]                = newReader
  given ChildReader[ZonedDateTime]         = newReader
  given ChildReader[Magnitude]             = newReader
  given ChildReader[RealQuantity]          = newReader
  given ChildReader[Int]                   = newReader
  given ChildReader[Double]                = newReader

  given [T](using ElementReader[T]): ChildReader[Option[T]] = (parent, name) => {
    for child <- parent.getChild(name)
    yield summon[ElementReader[T]](child)
  }

  given [T](using ElementReader[T]): ChildReader[Seq[T]] = (parent, name) => {
    for child <- parent.childreenOf(name)
    yield summon[ElementReader[T]](child)
  }

  def read[T: ChildReader](elementName: String, parent: Element): T =
    try summon[ChildReader[T]](parent, elementName)
    catch
      case cause =>
        throw java.lang.IllegalArgumentException(
          s"It's impossible to read element $elementName of ${parent.name}!",
          cause
        )

  private def newReader[T: ElementReader](parent: Element, name: String): T =
    summon[ElementReader[T]](parent.child(name))

@FunctionalInterface
private[quakeml] trait ChildReader[T] extends ((Element, String) => T)
