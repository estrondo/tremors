package graboid.quakeml

import com.fasterxml.aalto.AsyncXMLStreamReader.EVENT_INCOMPLETE
import graboid.Crawler
import graboid.GClock
import graboid.quakeml.Node.Child
import graboid.quakeml.Node.EmptyNodeMap
import graboid.quakeml.Node.Publishable
import graboid.quakeml.Node.Root
import graboid.quakeml.Node.Transparent
import java.time.ZonedDateTime
import javax.xml.stream.XMLStreamConstants.CHARACTERS
import javax.xml.stream.XMLStreamConstants.COMMENT
import javax.xml.stream.XMLStreamConstants.DTD
import javax.xml.stream.XMLStreamConstants.END_ELEMENT
import javax.xml.stream.XMLStreamConstants.START_DOCUMENT
import javax.xml.stream.XMLStreamConstants.START_ELEMENT
import quakeml.QuakeMLDetectedEvent
import scala.annotation.tailrec
import zio.Chunk
import zio.Task
import zio.ZIO
import zio.stream.ZStream

trait QuakeMLParser:

  def parse(stream: ZStream[Any, Throwable, Byte]): Task[ZStream[Any, Throwable, QuakeMLDetectedEvent]]

object QuakeMLParser:

  def apply(): QuakeMLParser = Impl()

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

    def endElement(now: ZonedDateTime): (State, Option[QuakeMLDetectedEvent]) =
      stack match
        case (CurrentNode(node, _, _, element)) :: tail if node.name == localName =>
          node match
            case _: Child =>
              (pushUp(element.get, tail), None)

            case _: Publishable =>
              (copy(stack = tail), Some(QuakeMLPublisher(now, element.get)))

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

  private class Impl extends QuakeMLParser:

    val root =
      val uncertaintyQuantityTemplate = Child("---")("value", "uncertainty")
      val creationInfoElement         = Child("creationInfo")(
        "agencyID",
        "agencyURI",
        "author",
        "authorURI",
        "creationTime",
        "version"
      )

      val commentElement = Child("comment")("text", "creationInfo")

      val magnitudeElement = Publishable("magnitude")(
        "stationCount",
        creationInfoElement,
        uncertaintyQuantityTemplate.withName("mag"),
        "type",
        commentElement,
        "originID",
        "methodID"
      )

      val timeElement      = uncertaintyQuantityTemplate.withName("time")
      val longitudeElement = uncertaintyQuantityTemplate.withName("longitude")
      val latitudeElement  = uncertaintyQuantityTemplate.withName("latitude")
      val depthElement     = uncertaintyQuantityTemplate.withName("depth")

      val originElement = Publishable("origin")(
        timeElement,
        longitudeElement,
        latitudeElement,
        depthElement,
        "depthType",
        "referenceSystemID",
        "methodID",
        "earthModelID",
        "type",
        "region",
        "evaluationMode",
        "evaluationStatus",
        commentElement,
        creationInfoElement
      )

      val descriptionElement = Child("description")("text", "type")
      val eventType          = Child("type", EmptyNodeMap)
      val eventElement       = Publishable("event")(
        descriptionElement,
        creationInfoElement,
        magnitudeElement.toChild(),
        originElement.toChild(),
        eventType,
        "typeCertainty",
        commentElement,
        "preferredMagnitudeID",
        "preferredOriginID"
      )
      val eventParameters    = Transparent("eventParameters", eventElement)
      Root("quakeml", 64, eventParameters)

    override def parse(stream: ZStream[Any, Throwable, Byte]): Task[ZStream[Any, Throwable, QuakeMLDetectedEvent]] =
      ZIO.attempt {
        stream
          .grouped(ChunkSize)
          .mapAccumZIO(State(root))(process)
          .flattenIterables
      }

    private def process(
        state: State,
        bytes: Chunk[Byte]
    ): Task[(State, Seq[QuakeMLDetectedEvent])] =
      for
        now    <- GClock.currentZonedDateTime()
        result <- ZIO.attempt {
                    state.reader.feed(bytes.toArray)
                    val (newState, published) = read(state, Vector.empty, now)
                    (newState, published)
                  }
      yield result

    @tailrec
    private def read(
        state: State,
        published: Seq[QuakeMLDetectedEvent],
        now: ZonedDateTime
    ): (State, Seq[QuakeMLDetectedEvent]) =
      state.reader.next() match
        case EVENT_INCOMPLETE =>
          (state, published)

        case DTD | START_DOCUMENT | COMMENT =>
          read(state, published, now)

        case START_ELEMENT =>
          read(state.startElement(), published, now)

        case END_ELEMENT =>
          val (newState, newPublished) = state.endElement(now)
          read(newState, published ++ newPublished, now)

        case CHARACTERS =>
          read(state.readText(), published, now)
