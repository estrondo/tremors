package graboid.quakeml

import com.fasterxml.aalto.AsyncByteArrayFeeder
import com.fasterxml.aalto.AsyncXMLStreamReader
import com.fasterxml.aalto.stax.InputFactoryImpl
import scala.collection.immutable.HashMap

private[quakeml] object Reader:
  type XmlReader = AsyncXMLStreamReader[AsyncByteArrayFeeder]

  def apply(): Reader = {
    val reader = InputFactoryImpl().createAsyncForByteArray()
    new Reader(reader)
  }

private[quakeml] class Reader(underlying: Reader.XmlReader):

  def attributes = HashMap.from(
    for index <- 0 until underlying.getAttributeCount()
    yield underlying.getAttributeName(index).getLocalPart() -> underlying.getAttributeValue(index)
  )

  def createElement() = Element(localName, attributes)

  def feed(bytes: Array[Byte]): Unit =
    underlying.getInputFeeder().feedInput(bytes, 0, bytes.length)

  def next(): Int = underlying.next()

  def localName = underlying.getLocalName()

  def text() = underlying.getText()
