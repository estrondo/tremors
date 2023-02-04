package graboid.protocol

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveAllCodecs

import java.time.Duration
import java.time.ZonedDateTime

import graboid.protocol.PublisherDescriptor
object GraboidCommand:

  given Codec[GraboidCommand] = deriveAllCodecs

sealed trait GraboidCommand(val id: String)

case class AddPublisher(override val id: String, descriptor: PublisherDescriptor) extends GraboidCommand(id)

case class RemovePublisher(override val id: String, publisherKey: String) extends GraboidCommand(id)

case class UpdatePublisher(override val id: String, descriptor: PublisherDescriptor) extends GraboidCommand(id)

case class RunAllPublishers(override val id: String) extends GraboidCommand(id)

case class RunPublisher(override val id: String, publisherKey: String) extends GraboidCommand(id)
