package graboid

import farango.FarangoDocumentCollection
import graboid.EventPublisher
import graboid.arango.ArangoConversion.given
import graboid.arango.ArangoRepository
import io.github.arainko.ducktape.into
import io.github.arainko.ducktape.Field
import zio.Cause
import zio.Task
import zio.Trace
import zio.ZIO

import java.lang.{Long => JLong}

trait EventPublisherRepository:

  def add(eventPublisher: EventPublisher): Task[EventPublisher]

  def remove(publisherKey: String): Task[Option[EventPublisher]]

  def update(publisherKey: String, update: EventPublisher.Update): Task[Option[EventPublisher]]

object EventPublisherRepository:

  def apply(collection: FarangoDocumentCollection): EventPublisherRepository =
    EventPublisherRepositoryImpl(collection)

  private[graboid] case class Document(
      _key: String,
      name: String,
      url: String,
      beginning: Long,
      ending: JLong,
      `type`: Int
  )

  private[graboid] case class UpdateDocument(
      name: String,
      url: String,
      beginning: Long,
      ending: JLong,
      `type`: Int
  )

  private[graboid] given Conversion[EventPublisher, Document] = publisher =>
    publisher
      .into[Document]
      .transform(
        Field.renamed(_._key, _.key)
      )

  private[graboid] given Conversion[Document, EventPublisher] = document =>
    document
      .into[EventPublisher]
      .transform(
        Field.renamed(_.key, _._key)
      )

  private[graboid] given Conversion[EventPublisher.Update, UpdateDocument] = update =>
    update
      .into[UpdateDocument]
      .transform()

  private class EventPublisherRepositoryImpl(collection: FarangoDocumentCollection) extends EventPublisherRepository:

    private val repository = ArangoRepository[Document](collection)

    override def add(eventPublisher: EventPublisher): Task[EventPublisher] =
      logUsage(
        message = s"Adding EventPublisher: ${eventPublisher.key}.",
        errorMessage = s"It was impossible to add EventPublisher: ${eventPublisher.key}."
      )(repository.add(eventPublisher))

    override def remove(publisherKey: String): Task[Option[EventPublisher]] =
      logUsage(
        message = s"Removing EventPublisher: $publisherKey.",
        errorMessage = s"It was impossible to remove EventPublisher: $publisherKey!"
      )(repository.remove(publisherKey))

    override def update(publisherKey: String, update: EventPublisher.Update): Task[Option[EventPublisher]] =
      logUsage(
        message = s"Updating EventPublishe: $publisherKey.",
        errorMessage = s"It was impossible to update EventPublisher: $publisherKey."
      )(repository.update[UpdateDocument, EventPublisher](publisherKey, update))

    def logUsage[T](message: => String, errorMessage: => String)(effect: Task[T])(using trace: Trace): Task[T] =
      for
        _      <- ZIO.logDebug(message)
        result <- effect
                    .mapError(GraboidException.unexpected(errorMessage))
                    .tapError(error => ZIO.logErrorCause(Cause.die(error)))
      yield result
