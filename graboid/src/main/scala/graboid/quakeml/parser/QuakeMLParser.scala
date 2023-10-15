package graboid.quakeml.parser

import com.fasterxml.aalto.AsyncXMLStreamReader
import com.fasterxml.aalto.stax.InputFactoryImpl
import graboid.GraboidException
import graboid.quakeml.reader.QuakeMLReader
import javax.xml.stream.XMLStreamConstants
import scala.annotation.tailrec
import scala.collection.immutable.HashMap
import tremors.quakeml.Event
import zio.Chunk
import zio.stream.ZStream

object QuakeMLParser:

  private val MaxLevel = 32

  def apply[R](stream: ZStream[R, Throwable, Byte], buffer: Int = 8 * 1024): ZStream[R, Throwable, Event] =
    parse(stream.grouped(buffer))

  def parse[R](stream: ZStream[R, Throwable, Chunk[Byte]]): ZStream[R, Throwable, Event] =
    ZStream
      .succeed(Parser())
      .flatMap { parser =>
        stream.flatMap(parser.parse)
      }

  private def children(nodes: Node*): Map[String, Node] =
    HashMap((for node <- nodes yield node.name -> node)*)

  private def child(name: String, children: Map[String, Node] = Map.empty): ChildNode =
    ChildNode(name, children)

  private def publishable(name: String, children: Map[String, Node] = Map.empty): PublishableNode =
    PublishableNode(name, children)

  sealed private trait Node:
    def name: String

    def getChild(name: String): Option[Node]

  abstract private class ParentNode(children: Map[String, Node]) extends Node:

    override def getChild(name: String): Option[Node] = children.get(name)

  abstract private class EditableNode(children: Map[String, Node]) extends ParentNode(children)

  private class Parser:

    private val parser = InputFactoryImpl().createAsyncForByteArray()
    private val root   =
      val realQuantityChildren = children(child("value"), child("uncertainty"))
      val timeQuantityChildren = children(child("value"), child("uncertainty"))
      val creationInfo         = child(
        "creationInfo",
        children(
          child("agencyID"),
          child("agencyURI"),
          child("author"),
          child("authorURI"),
          child("creationTime"),
          child("version")
        )
      )

      val comment = child("comment", children(child("text"), child("id"), creationInfo))

      val compositeTime = child(
        "compositeTime",
        children(child("year"), child("month"), child("day"), child("hour"), child("minute"), child("second"))
      )

      val originQuality = child(
        "quality",
        children(
          child("associatedPhaseCount"),
          child("usedPhaseCount"),
          child("associatedStationCount"),
          child("usedStationCount"),
          child("depthPhaseCount"),
          child("standardError"),
          child("azimuthalGap"),
          child("secondaryAzimuthalGap"),
          child("groundTruthLevel"),
          child("minimumDistance"),
          child("maximumDistance"),
          child("medianDistance")
        )
      )

      TransparentNode(
        "quakeml",
        children(
          TransparentNode(
            "eventParameters",
            children(
              publishable(
                "event",
                children(
                  child("preferredOriginID"),
                  child("preferredMagnitudeID"),
                  child("preferredFocalMechanismID"),
                  child("type"),
                  child("typeUncertainty"),
                  child("description", children(child("text"), child("type"))),
                  comment,
                  creationInfo,
                  child(
                    "origin",
                    children(
                      child("time", timeQuantityChildren),
                      child("longitude", realQuantityChildren),
                      child("latitude", realQuantityChildren),
                      child("depth", realQuantityChildren),
                      child("depthType"),
                      child("timeFixed"),
                      child("epicenterFixed"),
                      child("referenceSystemID"),
                      child("methodID"),
                      child("earthModelID"),
                      compositeTime,
                      originQuality,
                      child("type"),
                      child("region"),
                      child("evaluationMode"),
                      child("evaluationStatus"),
                      comment,
                      creationInfo
                    )
                  ),
                  child(
                    "magnitude",
                    children(
                      child("mag", realQuantityChildren),
                      child("type"),
                      child("originID"),
                      child("methodID"),
                      child("stationCount"),
                      child("azimuthalGap"),
                      child("evaluationMode"),
                      child("evaluationStatus"),
                      comment,
                      creationInfo
                    )
                  )
                )
              )
            )
          )
        )
      )

    private var stack: List[ElementBuilder] = List.empty

    def parse(chunk: Chunk[Byte]): ZStream[Any, Throwable, Event] =
      val buffer = chunk.toArray
      parser.getInputFeeder.feedInput(buffer, 0, buffer.length)
      try
        ZStream
          .fromIterable(next(Vector.empty))
          .mapZIO(QuakeMLReader.apply)
      catch case cause: Throwable => ZStream.fail(cause)

    @tailrec
    private def next(result: Vector[Element]): Vector[Element] =
      parser.next() match
        case XMLStreamConstants.START_ELEMENT =>
          next(startElement(result))

        case XMLStreamConstants.END_ELEMENT =>
          next(endElement(result))

        case XMLStreamConstants.CDATA | XMLStreamConstants.CHARACTERS =>
          next(readText(result))

        case AsyncXMLStreamReader.EVENT_INCOMPLETE =>
          result

        case XMLStreamConstants.START_DOCUMENT | XMLStreamConstants.COMMENT | XMLStreamConstants.DTD =>
          next(result)

    private def pushNewElement(elementName: String): ElementBuilder =
      def attributes = HashMap(
        (for i <- 0 until parser.getAttributeCount
        yield parser.getAttributeLocalName(i) -> parser.getAttributeValue(i))*
      )

      val newBuilder = stack match
        case head :: _ =>
          val newLevel = head.level + 1
          if (newLevel > MaxLevel)
            throw GraboidException.QuakeMLException(s"Too deep: $newLevel > $MaxLevel!")

          head.node
            .getChild(elementName)
            .map(ElementBuilder(elementName, _, newLevel, attributes))
            .getOrElse(ElementBuilder(elementName, IgnoreNode(elementName), newLevel))
        case _         =>
          if root.name == elementName then ElementBuilder(elementName, root, 0, attributes)
          else ElementBuilder(elementName, IgnoreNode(elementName), 0)

      stack = newBuilder :: stack
      newBuilder

    private def readText(result: Vector[Element]): Vector[Element] =
      stack match
        case head :: tail if head.node.isInstanceOf[EditableNode] =>
          stack = head.addText(parser.getText) :: tail
        case _                                                    =>

      result

    private def startElement(result: Vector[Element]): Vector[Element] =
      pushNewElement(parser.getLocalName)
      result

    private def endElement(result: Vector[Element]): Vector[Element] =
      stack match
        case head :: parent :: tail =>
          head.node match
            case _: ChildNode                       =>
              stack = parent.appendChild(head) :: tail
              result
            case _: PublishableNode                 =>
              stack = stack.tail
              result :+ head.build()
            case _: IgnoreNode | _: TransparentNode =>
              stack = stack.tail
              result
        case head :: _              =>
          stack = Nil
          head.node match
            case _: PublishableNode                                =>
              result :+ head.build()
            case _: ChildNode | _: IgnoreNode | _: TransparentNode =>
              result

        case _ =>
          throw GraboidException.QuakeMLException("Invalid element ending!")

  private case class ElementBuilder(
      name: String,
      node: Node,
      level: Int,
      attributes: Map[String, String] = Map.empty,
      content: Seq[String] = Vector.empty,
      children: Seq[Element] = Vector.empty
  ):

    def addText(text: String): ElementBuilder =
      copy(content = content :+ text)

    def appendChild(builder: ElementBuilder): ElementBuilder =
      copy(children = children :+ builder.build())

    def build(): Element =
      Element(name, attributes, Option.when(content.nonEmpty)(content.mkString), children)

  private case class PublishableNode(name: String, children: Map[String, Node]) extends EditableNode(children)

  private case class ChildNode(name: String, children: Map[String, Node]) extends EditableNode(children)

  private case class IgnoreNode(name: String) extends ParentNode(Map.empty)

  private case class TransparentNode(name: String, children: Map[String, Node]) extends ParentNode(children)
