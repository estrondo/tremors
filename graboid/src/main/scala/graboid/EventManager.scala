package graboid

import zio.Task
import graboid.Crawler.Info
import com.softwaremill.macwire.wire
import zio.kafka.producer.Producer
import zio.ULayer
import io.bullet.borer.Cbor
import graboid.Crawler.given
import zio.ZIO
import graboid.kafka.GraboidDetectedEvent
import zio.kafka.serde.Serde
import org.apache.kafka.clients.producer.RecordMetadata

trait EventManager:

  def register(info: Crawler.Info, publisher: Publisher, execution: CrawlerExecution): Task[Crawler.Info]

object EventManager:

  def apply(
      producerLayer: ULayer[Producer]
  ): EventManager =
    wire[EventManagerImpl]

  private class EventManagerImpl(
      producerLayer: ULayer[Producer]
  ) extends EventManager:

    def register(info: Info, publisher: Publisher, execution: CrawlerExecution): Task[Info] =
      for
        metadata <- produce(info)
        _        <- ZIO.logDebug(s"A new event has been sent do topic=${metadata.topic()}.")
      yield info

    private def produce(info: Info): Task[RecordMetadata] =
      for
        bytes    <- ZIO.fromTry(Cbor.encode(info).toByteArrayTry)
        metadata <-
          Producer
            .produce(GraboidDetectedEvent, s"event-${Crawler.getID(info)}", bytes, Serde.string, Serde.byteArray)
            .provideLayer(producerLayer)
      yield metadata
