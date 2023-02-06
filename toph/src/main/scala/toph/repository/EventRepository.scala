package toph.repository

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import toph.model.Event
import zio.Task
import zio.ZIO

trait EventRepository:

  def add(event: Event): Task[Event]

  def remove(key: String): Task[Option[Event]]

object EventRepository:

  def apply(collection: DocumentCollection): EventRepository =
    wire[Impl]

  private[repository] case class Document(
      _key: String
  )

  private[repository] given Conversion[Event, Document] =
    _.into[Document]
      .transform(Field.renamed(_._key, _.key))

  private[repository] given Conversion[Document, Event] =
    _.into[Event]
      .transform(Field.renamed(_.key, _._key))

  private class Impl(collection: DocumentCollection) extends EventRepository:

    override def add(event: Event): Task[Event] =
      collection
        .insert[Document](event)
        .tap(_ => ZIO.logDebug("A new event has been added."))
        .tapErrorCause(cause => ZIO.logErrorCause(s"I was impossible to add: ${event.key}!", cause))

    override def remove(key: String): Task[Option[Event]] =
      collection
        .remove[Document](key)
        .tap(opt =>
          if opt.isDefined then ZIO.logDebug(s"Event=${key} has been removed.")
          else ZIO.logDebug(s"There was not found event=$key to remove.")
        )
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to remove event=$key!", _))
