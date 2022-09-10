package tremors.graboid.quakeml

import scala.collection.immutable.HashMap

private[quakeml] sealed abstract class Schema(val name: String, val childreen: Vector[Schema]):

  protected val nameToSchema: Map[String, Schema] = HashMap.from(childreen.map(c => c.name -> c))

  def schemaOf(name: String): Option[Schema] = nameToSchema.get(name)

  def isChild: Boolean

private[quakeml] object Schema:

  class Publishable(name: String, childreen: Vector[Schema] = Vector.empty)
      extends Schema(name, childreen):

    def isChild: Boolean = false

  class Child(name: String, childreen: Vector[Schema] = Vector.empty)
      extends Schema(name, childreen):
    def isChild: Boolean = true

  given Conversion[String, Schema] = Child(_)

  def publishable(name: String)(childreen: Schema*) = Publishable(name, childreen.toVector)

  def child(name: String)(childreen: Schema*) = Child(name, childreen.toVector)
