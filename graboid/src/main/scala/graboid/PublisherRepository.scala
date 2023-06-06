package graboid

import farango.DocumentCollection
import farango.UpdateReturn
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.Transformer
import io.github.arainko.ducktape.into
import java.net.URL
import java.time.ZonedDateTime
import zio.Cause
import zio.Task
import zio.Trace
import zio.ZIO
import zio.stream.ZStream

trait PublisherRepository:

  def all: ZStream[Any, Throwable, Publisher]

  def add(publisher: Publisher): Task[Publisher]

  def get(key: String): Task[Option[Publisher]]

  def remove(publisherKey: String): Task[Option[Publisher]]

  def update(publisherKey: String, update: Publisher.Update): Task[Option[Publisher]]

object PublisherRepository:

  given Transformer[Crawler.Type, Int] = value => value.ordinal

  given Transformer[Int, Crawler.Type] = value => Crawler.Type.fromOrdinal(value)

  def apply(collection: DocumentCollection): PublisherRepository =
    Impl(collection)

  private[graboid] case class Document(
      _key: String,
      name: String,
      url: URL,
      beginning: ZonedDateTime,
      ending: Option[ZonedDateTime],
      `type`: Int
  )

  private[graboid] case class UpdateDocument(
      name: String,
      url: URL,
      beginning: Option[ZonedDateTime],
      ending: Option[ZonedDateTime],
      `type`: Int
  )

  private[graboid] given Conversion[Publisher, Document] = publisher =>
    publisher
      .into[Document]
      .transform(
        Field.renamed(_._key, _.key)
      )

  private[graboid] given Conversion[Document, Publisher] = document =>
    document
      .into[Publisher]
      .transform(
        Field.renamed(_.key, _._key)
      )

  private[graboid] given Conversion[Publisher.Update, UpdateDocument] = update =>
    update
      .into[UpdateDocument]
      .transform()

  private class Impl(collection: DocumentCollection) extends PublisherRepository:

    override def all: ZStream[Any, Throwable, Publisher] =
      collection.documents[Document]()

    override def add(publisher: Publisher): Task[Publisher] =
      logUsage(
        message = s"Adding Publisher: ${publisher.key}.",
        errorMessage = s"It was impossible to add Publisher: ${publisher.key}."
      )(collection.insert[Document](publisher))

    override def get(key: String): Task[Option[Publisher]] =
      collection.get[Document](key)

    override def remove(publisherKey: String): Task[Option[Publisher]] =
      logUsage(
        message = s"Removing Publisher: $publisherKey.",
        errorMessage = s"It was impossible to remove Publisher: $publisherKey!"
      )(collection.remove[Document](publisherKey))

    override def update(publisherKey: String, update: Publisher.Update): Task[Option[Publisher]] =
      logUsage(
        message = s"Updating Publisher: $publisherKey.",
        errorMessage = s"It was impossible to update Publisher: $publisherKey."
      )(collection.update[UpdateDocument, Document](publisherKey, update, UpdateReturn.New))

    def logUsage[T](message: => String, errorMessage: => String)(effect: Task[T])(using trace: Trace): Task[T] =
      for
        _      <- ZIO.logDebug(message)
        result <- effect
                    .mapError(GraboidException.unexpected(errorMessage))
                    .tapError(error => ZIO.logErrorCause(Cause.die(error)))
      yield result
