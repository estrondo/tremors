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
import zkafka.KafkaProducer
import toph.kafka.TophEventJournalTopic

trait EventPublisher extends KafkaProducer[(Event, Seq[(Epicentre, Option[Hypocentre])]), EventJournalMessage]

object EventPublisher:

  def apply(): EventPublisher =
    wire[Impl]

  private class Impl() extends EventPublisher:
    override def accept(
        key: String,
        value: (Event, Seq[(Epicentre, Option[Hypocentre])])
    ): Task[Seq[KafkaMessage[EventJournalMessage]]] =
      val (event, centres) = value

      (for newEvent <- EventJournalMessageConverter.newEventFrom(event, centres)
      yield Seq(KafkaMessage(newEvent, Some(event.key), TophEventJournalTopic)))
        .tap(_ => ZIO.logDebug(s"Event key=${event.key} was published."))
        .tapErrorCause(ZIO.logWarningCause(s"I was impossible to publish event=${event.key}!", _))
