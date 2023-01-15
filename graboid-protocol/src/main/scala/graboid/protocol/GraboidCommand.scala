package graboid.protocol

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveAllCodecs

import java.time.Duration
import java.time.ZonedDateTime

object GraboidCommand:

  given Codec[GraboidCommand] = deriveAllCodecs

sealed trait GraboidCommand(val id: String)

case class AddEventPublisher(override val id: String, descriptor: EventPublisherDescriptor) extends GraboidCommand(id)

case class RemoveEventPublisher(override val id: String, publisherKey: String) extends GraboidCommand(id)

case class UpdateEventPublisher(override val id: String, descriptor: EventPublisherDescriptor)
    extends GraboidCommand(id)
