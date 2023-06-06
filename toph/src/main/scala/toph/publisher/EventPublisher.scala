package toph.publisher

import com.softwaremill.macwire.wire
import toph.converter.EventJournalMessageConverter
import toph.kafka.TophEventJournalTopic
import toph.message.protocol.EventJournalMessage
import toph.model.data.EventData
import toph.model.data.HypocentreData
import toph.model.data.MagnitudeData
import zio.Task
import zio.ZIO
import zkafka.KafkaMessage
import zkafka.KafkaProducer

trait EventPublisher extends KafkaProducer[(EventData, Seq[HypocentreData], Seq[MagnitudeData]), EventJournalMessage]

object EventPublisher:

  def apply(): EventPublisher =
    wire[Impl]

  private class Impl() extends EventPublisher:
    override def accept(
        key: String,
        tuple: (EventData, Seq[HypocentreData], Seq[MagnitudeData])
    ): Task[Seq[KafkaMessage[EventJournalMessage]]] =
      val (event, hypocentres, magnitudes) = tuple

      (for newEvent <- EventJournalMessageConverter.newEventFrom(event, hypocentres)
      yield Seq(KafkaMessage(newEvent, Some(event.key), TophEventJournalTopic)))
        .tap(_ => ZIO.logDebug(s"Event key=${event.key} was published."))
        .tapErrorCause(ZIO.logWarningCause(s"I was impossible to publish event=${event.key}!", _))
