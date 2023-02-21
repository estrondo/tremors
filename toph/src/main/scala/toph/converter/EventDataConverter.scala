package toph.converter

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import quakeml.QuakeMLEvent
import toph.model.data.EventData
import zio.Task
import zio.ZIO

object EventDataConverter:

  def fromQEvent(event: QuakeMLEvent): Task[EventData] = ZIO.attempt {
    event
      .into[EventData]
      .transform(
        Field.const(_.key, event.publicID.uri),
        Field.const(_.preferedMagnitudeKey, event.preferredMagnitudeID: Option[String]),
        Field.const(_.preferredOriginKey, event.preferredOriginID: Option[String]),
        Field.const(_.originKey, event.origin.map(_.publicID.uri)),
        Field.const(_.magnitudeKey, event.magnitude.map(_.publicID.uri)),
        Field.const(_.typeUncertainty, event.typeCertainty.map(_.value)),
        Field.const(_.`type`, event.`type`.map(_.value)),
        Field.const(_.description, event.description.map(_.text))
      )
  }
