package graboid

import _root_.quakeml.QuakeMLDetectedEvent
import cbor.quakeml.given
import com.softwaremill.macwire.wire
import graboid.Crawler.given
import graboid.kafka.GraboidDetectedEvent
import io.bullet.borer.Cbor
import org.apache.kafka.clients.producer.RecordMetadata
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde

trait EventManager:

  def register(
      event: QuakeMLDetectedEvent,
      publisher: Publisher,
      execution: CrawlerExecution
  ): Task[QuakeMLDetectedEvent]

object EventManager:

  def apply(
      producerLayer: TaskLayer[Producer]
  ): EventManager =
    wire[Impl]

  private class Impl(
      producerLayer: TaskLayer[Producer]
  ) extends EventManager:

    def register(
        event: QuakeMLDetectedEvent,
        publisher: Publisher,
        execution: CrawlerExecution
    ): Task[QuakeMLDetectedEvent] =
      for
        metadata <- produce(event)
        _        <- ZIO.logDebug(s"A new event has been sent to topic=${metadata.topic()}.")
      yield event

    private def produce(event: QuakeMLDetectedEvent): Task[RecordMetadata] =
      for
        bytes    <- ZIO.fromTry(Cbor.encode(event).toByteArrayTry)
        metadata <-
          Producer
            .produce(GraboidDetectedEvent, s"event-${event.id}", bytes, Serde.string, Serde.byteArray)
            .provideLayer(producerLayer)
      yield metadata
