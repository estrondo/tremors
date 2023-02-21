package graboid.quakeml

import quakeml.QuakeMLComment
import quakeml.QuakeMLCreationInfo
import quakeml.QuakeMLEvent
import quakeml.QuakeMLMagnitude
import quakeml.QuakeMLOrigin
import quakeml.QuakeMLRealQuantity
import quakeml.QuakeMLResourceReference
import quakeml.QuakeMLTimeQuantity

import java.time.ZonedDateTime

private[quakeml] object ChildReader:

  given ChildReader[QuakeMLResourceReference]     = newReader
  given ChildReader[QuakeMLEvent.DescriptionType] = newReader
  given ChildReader[QuakeMLEvent.Type]            = newReader
  given ChildReader[QuakeMLEvent.TypeCertainty]   = newReader
  given ChildReader[QuakeMLEvent.Description]     = newReader
  given ChildReader[QuakeMLComment]               = newReader
  given ChildReader[QuakeMLCreationInfo]          = newReader
  given ChildReader[String]                       = newReader
  given ChildReader[ZonedDateTime]                = newReader
  given ChildReader[QuakeMLTimeQuantity]          = newReader
  given ChildReader[QuakeMLOrigin]                = newReader
  given ChildReader[QuakeMLMagnitude]             = newReader
  given ChildReader[QuakeMLRealQuantity]          = newReader
  given ChildReader[Int]                          = newReader
  given ChildReader[Double]                       = newReader

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
