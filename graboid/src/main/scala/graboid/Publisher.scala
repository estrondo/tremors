package graboid

import graboid.protocol.PublisherDescriptor
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import java.net.URL
import java.time.ZonedDateTime
import zio.logging.LogAnnotation

case class Publisher(
    key: String,
    name: String,
    url: URL,
    beginning: ZonedDateTime,
    ending: Option[ZonedDateTime],
    `type`: Crawler.Type
)

object Publisher:

  val Annotation = LogAnnotation[Publisher]("Publisher", (_, x) => x, x => s"key=${x.key}, name=${x.name}")

  case class Invalid(publisher: Publisher, cause: Seq[Cause])

  case class Cause(reason: String)

  case class Update(
      name: String,
      url: URL,
      beginning: ZonedDateTime,
      ending: Option[ZonedDateTime],
      `type`: Crawler.Type
  )

  def from(descriptor: PublisherDescriptor): Publisher =
    descriptor
      .into[Publisher]
      .transform(
        Field.const(_.url, URL(descriptor.location)),
        Field.const(_.`type`, Crawler.Type.valueOf(descriptor.`type`))
      )

  def updateFrom(descriptor: PublisherDescriptor): Update =
    descriptor
      .into[Update]
      .transform(
        Field.const(_.url, URL(descriptor.location)),
        Field.const(_.`type`, Crawler.Type.valueOf(descriptor.`type`))
      )
