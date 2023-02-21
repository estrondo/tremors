package toph.repository

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import zio.Task
import zio.ZIO

import java.time.ZonedDateTime
import farango.data.Key
import toph.model.data.CreationInfoData
import toph.model.data.EventData

trait EventDataRepository:

  def add(event: EventData): Task[EventData]

  def remove(key: String): Task[Option[EventData]]

object EventDataRepository:

  def apply(collection: DocumentCollection): EventDataRepository =
    wire[Impl]

  private[repository] case class CreationInfoDocument(
      agencyID: Option[String],
      agencyURI: Option[String],
      author: Option[String],
      creationTime: Option[ZonedDateTime],
      version: Option[String]
  )

  private[repository] case class Document(
      _key: Key,
      preferredOriginKey: Option[Key],
      preferedMagnitudeKey: Option[Key],
      `type`: Option[String],
      typeUncertainty: Option[String],
      description: Seq[String],
      comment: Seq[String],
      creationInfo: Option[CreationInfoDocument],
      originKey: Seq[Key],
      magnitudeKey: Seq[Key]
  )

  private[repository] given Conversion[CreationInfoData, CreationInfoDocument] = creationInfo =>
    creationInfo
      .into[CreationInfoDocument]
      .transform()

  private[repository] given Conversion[CreationInfoDocument, CreationInfoData] = document =>
    document
      .into[CreationInfoData]
      .transform()

  private[repository] given Conversion[EventData, Document] = event =>
    event
      .into[Document]
      .transform(Field.const(_._key, event.key: Key))

  private[repository] given Conversion[Document, EventData] = document =>
    document
      .into[EventData]
      .transform(Field.const(_.key, document._key: String))

  private class Impl(collection: DocumentCollection) extends EventDataRepository:

    override def add(event: EventData): Task[EventData] =
      collection
        .insert[Document](event)
        .tap(_ => ZIO.logDebug("A new event has been added."))
        .tapErrorCause(cause => ZIO.logErrorCause(s"I was impossible to add: ${event.key}!", cause))

    override def remove(key: String): Task[Option[EventData]] =
      collection
        .remove[Document](Key.safe(key))
        .tap(opt =>
          if opt.isDefined then ZIO.logDebug(s"Event=${key} has been removed.")
          else ZIO.logDebug(s"There was not found event=$key to remove.")
        )
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to remove event=$key!", _))
