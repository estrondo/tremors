package graboid

import graboid.protocol.EventPublisherDescriptor
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into

import java.net.URL
import java.time.ZonedDateTime

case class EventPublisher(
    key: String,
    name: String,
    url: URL,
    beginning: ZonedDateTime,
    ending: Option[ZonedDateTime],
    `type`: Crawler.Type
)

object EventPublisher:

  case class Invalid(publisher: EventPublisher, cause: Seq[Cause])

  case class Cause(reason: String)

  case class Update(
      name: String,
      url: URL,
      beginning: ZonedDateTime,
      ending: Option[ZonedDateTime],
      `type`: Crawler.Type
  )

  def from(descriptor: EventPublisherDescriptor): EventPublisher =
    descriptor
      .into[EventPublisher]
      .transform(
        Field.const(_.url, URL(descriptor.location)),
        Field.const(_.`type`, Crawler.Type.valueOf(descriptor.`type`))
      )

  def updateFrom(descriptor: EventPublisherDescriptor): Update =
    descriptor
      .into[Update]
      .transform(
        Field.const(_.url, URL(descriptor.location)),
        Field.const(_.`type`, Crawler.Type.valueOf(descriptor.`type`))
      )
