package graboid

import com.softwaremill.macwire.wire
import graboid.PublisherManager.Validator
import zio.IO
import zio.Task
import zio.ZIO
import zio.stream.ZStream
trait PublisherManager:

  def add(publisher: Publisher): Task[Publisher]

  def get(publisherKey: String): Task[Option[Publisher]]

  def getActives(): Task[ZStream[Any, Throwable, Publisher]]

  def remove(publisherKey: String): Task[Option[Publisher]]

  def update(publisherKey: String, update: Publisher.Update): Task[Option[Publisher]]

object PublisherManager:

  trait Validator extends ((Publisher) => IO[Publisher.Invalid, Publisher])

  def apply(repository: PublisherRepository, validator: Validator): PublisherManager =
    wire[Impl]

private[graboid] class Impl(
    repository: PublisherRepository,
    validator: Validator
) extends PublisherManager:

  def get(publisherKey: String): Task[Option[Publisher]] =
    repository.get(publisherKey)

  def getActives(): Task[ZStream[Any, Throwable, Publisher]] =
    ZIO.attempt(repository.all)

  def add(publisher: Publisher): Task[Publisher] =
    for
      validated <- validator(publisher).mapError(handleInvalidPublisher)
      stored    <- repository
                     .add(validated)
                     .mapError(illegalState(s"It was impossible to add the publisher ${publisher.name} into repository!"))
    yield stored

  def remove(publisherKey: String): Task[Option[Publisher]] =
    repository.remove(publisherKey).mapError(illegalState(s"It was impossible to remove $publisherKey!"))

  def update(publisherKey: String, update: Publisher.Update): Task[Option[Publisher]] =
    repository.update(publisherKey, update).mapError(illegalState(s"It was impossible to update $publisherKey!"))

  private def handleInvalidPublisher(invalid: Publisher.Invalid) =
    GraboidException.IllegalRequest(
      s"Invalid Publisher ${invalid.publisher.name}!",
      GraboidException.MultipleCause(
        "There've been detected the following problems!",
        invalid.cause.map(convertToException)
      )
    )

  private def illegalState(message: String)(cause: Throwable) =
    GraboidException.IllegalState(message, cause)

  private def convertToException(cause: Publisher.Cause): GraboidException.Invalid =
    GraboidException.Invalid(cause.reason)
