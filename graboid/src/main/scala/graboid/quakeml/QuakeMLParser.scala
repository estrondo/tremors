package graboid.quakeml

import com.fasterxml.aalto.AsyncXMLStreamReader.EVENT_INCOMPLETE
import graboid.Crawler
import graboid.quakeml.Node.Child
import graboid.quakeml.Node.EmptyNodeMap
import graboid.quakeml.Node.Publishable
import graboid.quakeml.Node.Root
import graboid.quakeml.Node.Transparent
import zio.Chunk
import zio.Task
import zio.ZIO
import zio.stream.ZStream

import javax.xml.stream.XMLStreamConstants.CHARACTERS
import javax.xml.stream.XMLStreamConstants.DTD
import javax.xml.stream.XMLStreamConstants.END_ELEMENT
import javax.xml.stream.XMLStreamConstants.START_DOCUMENT
import javax.xml.stream.XMLStreamConstants.START_ELEMENT
import scala.annotation.tailrec

trait QuakeMLParser:

  def parse(stream: ZStream[Any, Throwable, Byte]): Task[ZStream[Any, Throwable, Crawler.Info]]

object QuakeMLParser:

  def apply(): QuakeMLParser = QuakeMLParserImpl()

  private val ChunkSize = 1024

  private case class State(
      root: Root,
      reader: Reader = Reader(),
      stack: List[CurrentNode] = Nil
  ):

    def localName = reader.localName

    def startElement(): State =
      stack match
        case current :: _ =>
          val nextNode  = current.node.nodeFor(localName)
          val nextDepth = current.depth + 1

          val element = nextNode match
            case _: Child | _: Publishable =>
              Some(reader.createElement())
            case _                         =>
              // println(s"${"-" * nextDepth}> skiping ${nextNode.name}.")
              None

          copy(stack = CurrentNode(nextNode, root, nextDepth, element) :: stack)

        case _ if root.name == localName =>
          copy(stack = CurrentNode(root, root, 0, None) :: stack)

        case _ =>
          throw IllegalStateException(s"Invalid root element $localName!")

    def endElement(): (State, Option[Crawler.Info]) =
      stack match
        case (CurrentNode(node, _, _, element)) :: tail if node.name == localName =>
          node match
            case _: Child =>
              (pushUp(element.get, tail), None)

            case _: Publishable =>
              (copy(stack = tail), Some(Publisher(element.get)))

            case _ =>
              (copy(stack = tail), None)

        case _ =>
          throw IllegalStateException("It's imposible to endElement!")

    private def pushUp(child: Element, newStack: List[CurrentNode]): State =
      newStack match
        case (current @ CurrentNode(_, _, _, Some(parent))) :: tail =>
          copy(stack = current.copy(element = Some(parent.appendChild(child))) :: tail)
        case _                                                      =>
          throw IllegalStateException("It's impossible to pushUp!")

    def readText(): State =
      stack match
        case (current @ CurrentNode(_, _, _, Some(element))) :: tail =>
          copy(stack = current.copy(element = Some(element.appendText(reader.text()))) :: tail)

        case _ =>
          this

  private class QuakeMLParserImpl extends QuakeMLParser:

    val root =
      val realQuantityAttributes = Seq[Child]("value", "uncertainty")
      val creationInfoElement    = Child("creationInfo")(
        "agencyID",
        "agencyURI",
        "author",
        "authorURI",
        "creationTime",
        "version"
      )

      val magnitudeElement   = Publishable("magnitude")(
        "stationCount",
        creationInfoElement,
        Child("mag")(realQuantityAttributes*)
      )
      val descriptionElement = Child("description")("text", "type")
      val eventType          = Child("type", EmptyNodeMap)
      val eventElement       = Publishable("event")(
        descriptionElement,
        eventType,
        creationInfoElement,
        magnitudeElement.toChild()
      )
      val eventParameters    = Transparent("eventParameters", eventElement)
      Root("quakeml", 64, eventParameters)

    override def parse(stream: ZStream[Any, Throwable, Byte]): Task[ZStream[Any, Throwable, Crawler.Info]] =
      ZIO.attempt {
        stream
          .grouped(ChunkSize)
          .mapAccumZIO(None)(process)
          .flattenIterables
      }

    private def process(
        state: Option[State],
        bytes: Chunk[Byte]
    ): Task[(Option[State], Seq[Crawler.Info])] = ZIO.attempt {
      val currentState          = state getOrElse State(root)
      currentState.reader.feed(bytes.toArray)
      val (newState, published) = read(currentState, Vector.empty)
      (Some(newState), published)
    }

    @tailrec
    private def read(state: State, published: Seq[Crawler.Info]): (State, Seq[Crawler.Info]) =
      state.reader.next() match
        case EVENT_INCOMPLETE =>
          (state, published)

        case DTD | START_DOCUMENT =>
          read(state, published)

        case START_ELEMENT =>
          read(state.startElement(), published)

        case END_ELEMENT =>
          val (newState, newPublished) = state.endElement()
          read(newState, published ++ newPublished)

        case CHARACTERS =>
          read(state.readText(), published)
