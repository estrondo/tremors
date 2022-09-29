package tremors.graboid.quakeml

import scala.collection.immutable.HashMap

private[quakeml] object Node:

  type Map = scala.collection.immutable.Map[String, Node]

  val EmptyNodeMap: Map = Map.empty

  abstract class Container(name: String, val nodeMap: Map) extends Node(name):

    override def nodeFor(childName: String): Node =
      nodeMap.get(childName) match
        case Some(childLevel) => childLevel
        case None             => Skip(childName)

  class Root(name: String, val max: Int, content: Map)
      extends Container(name, content): // quakeml

    require(max > 0, "Max must be positive!")

  class Transparent(name: String, content: Map)
      extends Container(name, content) // eventParameters

  class Publishable(name: String, content: Map)
      extends Container(name, content): // event, origin, magnitude

    def this(name: String)(nodeMap: Node*) = this(name, nodeMap)

    def toChild(): Child = Child(name, content)

  class Child(name: String, content: Map)
      extends Container(name, content): // some ..............

    def this(name: String)(nodeMap: Node*) = this(name, nodeMap)

  class Skip(name: String) extends Node(name): // others

    def nodeFor(childName: String): Node = Skip(childName)

  given Conversion[Node, Map] = level => HashMap(level.name -> level)

  given Conversion[String, Child] = name => Child(name, EmptyNodeMap)

  given Conversion[Iterable[Node], Map] = iterable =>
    HashMap.from {
      for level <- iterable
      yield level.name -> level
    }

private[quakeml] sealed abstract class Node(val name: String):

  def nodeFor(childName: String): Node

case class CurrentNode(
    val node: Node,
    val root: Node.Root,
    val depth: Int,
    val element: Option[Element]
):
  require(depth >= 0, "Invalid negative depth!")
  require(depth <= root.max, s"Max depth ${root.max} has been reached on ${node.name}!")
