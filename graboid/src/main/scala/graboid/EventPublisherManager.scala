package graboid

import com.softwaremill.macwire.wire
import farango.FarangoDocumentCollection
import zio.{Task, IO}
import graboid.EventPublisherManager.Validator

trait EventPublisherManager:

  def add(publisher: EventPublisher): Task[EventPublisher]

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
      stored    <- repository.add(validated).mapError(handleRepositoryError(publisher))
    yield stored

  private def handleInvalidPublisher(invalid: EventPublisher.Invalid) =
    GraboidException.IllegalRequest(
      s"Invalid EventPublisher ${invalid.publisher.name}!",
      GraboidException.MultipleCause(
        "There've been detected the following problems!",
        invalid.cause.map(convertToException)
      )
    )

  private def handleRepositoryError(publisher: EventPublisher)(cause: Throwable) =
    GraboidException.IllegalState(
      s"It was impossible to add the publisher ${publisher.name} into repository",
      cause
    )

  private def convertToException(cause: EventPublisher.Cause): GraboidException.Invalid =
    GraboidException.Invalid(cause.reason)
