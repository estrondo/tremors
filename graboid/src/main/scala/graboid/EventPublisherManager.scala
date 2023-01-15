package graboid

import com.softwaremill.macwire.wire
import farango.FarangoDocumentCollection
import graboid.EventPublisherManager.Validator
import zio.IO
import zio.Task

trait EventPublisherManager:

  def add(publisher: EventPublisher): Task[EventPublisher]

  def remove(publisherKey: String): Task[Option[EventPublisher]]

  def update(publisherKey: String, update: EventPublisher.Update): Task[Option[EventPublisher]]

object EventPublisherManager:

  trait Validator extends ((EventPublisher) => IO[EventPublisher.Invalid, EventPublisher])

  def apply(repository: EventPublisherRepository, validator: Validator): EventPublisherManager =
    wire[EventPublisherManagerImpl]

private[graboid] class EventPublisherManagerImpl(
    repository: EventPublisherRepository,
    validator: Validator
) extends EventPublisherManager:

  def add(publisher: EventPublisher): Task[EventPublisher] =
    for
      validated <- validator(publisher).mapError(handleInvalidPublisher)
      stored    <- repository
                     .add(validated)
                     .mapError(illegalState(s"It was impossible to add the publisher ${publisher.name} into repository!"))
    yield stored

  def remove(publisherKey: String): Task[Option[EventPublisher]] =
    repository.remove(publisherKey).mapError(illegalState(s"It was impossible to remove $publisherKey!"))

  def update(publisherKey: String, update: EventPublisher.Update): Task[Option[EventPublisher]] =
    repository.update(publisherKey, update).mapError(illegalState(s"It was impossible to update $publisherKey!"))

  private def handleInvalidPublisher(invalid: EventPublisher.Invalid) =
    GraboidException.IllegalRequest(
      s"Invalid EventPublisher ${invalid.publisher.name}!",
      GraboidException.MultipleCause(
        "There've been detected the following problems!",
        invalid.cause.map(convertToException)
      )
    )

  private def illegalState(message: String)(cause: Throwable) =
    GraboidException.IllegalState(message, cause)

  private def convertToException(cause: EventPublisher.Cause): GraboidException.Invalid =
    GraboidException.Invalid(cause.reason)
