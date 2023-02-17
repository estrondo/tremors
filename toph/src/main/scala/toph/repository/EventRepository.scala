package toph.repository

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import toph.model.CreationInfo
import toph.model.Event
import zio.Task
import zio.ZIO

import java.time.ZonedDateTime
import farango.data.Key

trait EventRepository:

  def add(event: Event): Task[Event]

  def remove(key: String): Task[Option[Event]]

object EventRepository:

  def apply(collection: DocumentCollection): EventRepository =
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

  private[repository] given Conversion[CreationInfo, CreationInfoDocument] = creationInfo =>
    creationInfo
      .into[CreationInfoDocument]
      .transform()

  private[repository] given Conversion[CreationInfoDocument, CreationInfo] = document =>
    document
      .into[CreationInfo]
      .transform()

  private[repository] given Conversion[Event, Document] = event =>
    event
      .into[Document]
      .transform(Field.const(_._key, event.key: Key))

  private[repository] given Conversion[Document, Event] = document =>
    document
      .into[Event]
      .transform(Field.const(_.key, document._key: String))

  private class Impl(collection: DocumentCollection) extends EventRepository:

    override def add(event: Event): Task[Event] =
      collection
        .insert[Document](event)
        .tap(_ => ZIO.logDebug("A new event has been added."))
        .tapErrorCause(cause => ZIO.logErrorCause(s"I was impossible to add: ${event.key}!", cause))

    override def remove(key: String): Task[Option[Event]] =
      collection
        .remove[Document](Key.safe(key))
        .tap(opt =>
          if opt.isDefined then ZIO.logDebug(s"Event=${key} has been removed.")
          else ZIO.logDebug(s"There was not found event=$key to remove.")
        )
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to remove event=$key!", _))
