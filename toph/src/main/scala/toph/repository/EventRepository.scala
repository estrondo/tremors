package toph.repository

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.ArangoConversion
import farango.data.ArangoConversion.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import toph.model.Event
import zio.Task
import zio.ZIO

import java.lang.{Long => JLong}

trait EventRepository:

  def add(event: Event): Task[Event]

  def remove(key: String): Task[Option[Event]]

object EventRepository:

  def apply(collection: DocumentCollection): EventRepository =
    wire[Impl]

  private[repository] case class CreationInfoDocument(
      agencyID: String,
      agencyURI: String,
      author: String,
      creationTime: JLong,
      version: String
  )

  private[repository] case class Document(
      _key: String,
      preferredOriginKey: String,
      preferedMagnitudeKey: String,
      `type`: String,
      typeUncertainty: String,
      description: Seq[String],
      comment: Seq[String],
      creationInfo: CreationInfoDocument,
      originKey: Seq[String],
      magnitudeKey: Seq[String]
  )

  private[repository] given Conversion[Event, Document] = event =>
    event
      .into[Document]
      .transform(Field.const(_._key, ArangoConversion.convertToKey(event.key)))

  private[repository] given Conversion[Document, Event] = document =>
    document
      .into[Event]
      .transform(Field.const(_.key, ArangoConversion.convertFromKey(document._key)))

  private class Impl(collection: DocumentCollection) extends EventRepository:

    override def add(event: Event): Task[Event] =
      collection
        .insert[Document](event)
        .tap(_ => ZIO.logDebug("A new event has been added."))
        .tapErrorCause(cause => ZIO.logErrorCause(s"I was impossible to add: ${event.key}!", cause))

    override def remove(key: String): Task[Option[Event]] =
      collection
        .remove[Document](ArangoConversion.convertToKey(key))
        .tap(opt =>
          if opt.isDefined then ZIO.logDebug(s"Event=${key} has been removed.")
          else ZIO.logDebug(s"There was not found event=$key to remove.")
        )
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to remove event=$key!", _))
