package graboid

import farango.FarangoDocumentCollection
import graboid.arango.ArangoConversion.given
import graboid.arango.ArangoRepository
import io.github.arainko.ducktape.to
import zio.Task
import zio.ZIO
import zio.config.derivation.name

import java.lang.{Long => JLong}

import GraboidException.unexpected

import graboid.EventPublisher
trait EventPublisherRepository:

  def add(eventPublisher: EventPublisher): Task[EventPublisher]

object EventPublisherRepository:

  def apply(collection: FarangoDocumentCollection): EventPublisherRepository =
    EventPublisherRepositoryImpl(collection)

  private[graboid] case class Document(
      key: String,
      name: String,
      url: String,
      beginning: Long,
      ending: JLong,
      `type`: Int
  )

  private[graboid] given Conversion[EventPublisher, Document] = publisher => publisher.to[Document]

  private[graboid] given Conversion[Document, EventPublisher] = stored => stored.to[EventPublisher]

  private class EventPublisherRepositoryImpl(collection: FarangoDocumentCollection)
      extends EventPublisherRepository:

    private val repository = ArangoRepository[Document](collection)

    override def add(eventPublisher: EventPublisher): Task[EventPublisher] =
      for
        _     <- ZIO.logInfo(s"Adding EventPublisher: ${eventPublisher.name}.")
        added <-
          repository
            .add(eventPublisher)
            .mapError(unexpected(s"It was impossible to add: ${eventPublisher.name}"))
      yield added
