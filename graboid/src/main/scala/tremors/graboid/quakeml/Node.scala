package tremors.graboid.quakeml

import scala.collection.immutable.HashMap

private[quakeml] object Node:

  type NodeMap = Map[String, Node]

  val EmptyNodeMap: NodeMap = Map.empty

  abstract class Container(name: String, val nodeMap: NodeMap) extends Node(name):

    override def nodeFor(childName: String): Node =
      nodeMap.get(childName) match
        case Some(childLevel) => childLevel
        case None             => Skip(childName)

  class Root(name: String, val max: Int, content: NodeMap)
      extends Container(name, content): // quakeml

    require(max > 0, "Max must be positive!")

  class Transparent(name: String, content: NodeMap)
      extends Container(name, content) // eventParameters

  class Publishable(name: String, content: NodeMap)
      extends Container(name, content): // event, origin, magnitude

    def this(name: String)(nodeMap: Node*) = this(name, nodeMap)

    def toChild(): Child = Child(name, content)

  class Child(name: String, content: NodeMap)
      extends Container(name, content): // some ..............

    def this(name: String)(nodeMap: Node*) = this(name, nodeMap)

  class Skip(name: String) extends Node(name): // others

    def nodeFor(childName: String): Node = Skip(childName)

  given Conversion[Node, NodeMap] = level => HashMap(level.name -> level)

  given Conversion[String, Child] = name => Child(name, EmptyNodeMap)

  given Conversion[Iterable[Node], NodeMap] = iterable =>
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
