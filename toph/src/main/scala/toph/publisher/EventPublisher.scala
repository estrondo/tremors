package toph.publisher

import com.softwaremill.macwire.wire
import toph.converter.EventJournalMessageConverter
import toph.message.protocol.EventJournalMessage
import toph.model.Epicentre
import toph.model.Event
import toph.model.Hypocentre
import zio.Queue
import zio.Task
import zio.ZIO
import zio.stream.ZStream
import zkafka.KafkaMessage

trait EventPublisher:

  def publish(
      event: Event,
      origins: Seq[(Epicentre, Option[Hypocentre])]
  ): Task[(Event, Seq[(Epicentre, Option[Hypocentre])])]

object EventPublisher:

  def apply(limit: Int = 1000): Task[(EventPublisher, ZStream[Any, Throwable, EventJournalMessage])] =
    for queue <- Queue.bounded[EventJournalMessage](limit)
    yield (wire[Impl], ZStream.fromQueue(queue))

  private class Impl(queue: Queue[EventJournalMessage]) extends EventPublisher:

    override def publish(
        event: Event,
        centres: Seq[(Epicentre, Option[Hypocentre])]
    ): Task[(Event, Seq[(Epicentre, Option[Hypocentre])])] =
      (for
        newEvent <- EventJournalMessageConverter.newEventFrom(event, centres)
        _        <- queue.offer(newEvent)
      yield (event, centres))
        .tap(_ => ZIO.logDebug(s"Event key=${event.key} was published."))
        .tapErrorCause(ZIO.logWarningCause(s"I was impossible to publish event=${event.key}!", _))
