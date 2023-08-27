package graboid

import com.softwaremill.macwire.wire
import graboid.http.UpdateQueryParam
import graboid.quakeml.parser.QuakeMLParser
import io.bullet.borer.Cbor
import org.apache.kafka.clients.producer.ProducerRecord
import tremors.ExternalServiceException
import tremors.quakeml.Event
import tremors.zio.http.HttpChecker
import zio.RIO
import zio.ZIO
import zio.http.Client
import zio.http.Request
import zio.http.Response
import zio.http.URL
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.stream.ZStream
import zio.stream.ZStreamAspect

trait Crawler:

  def apply[Q](query: Q)(using UpdateQueryParam[Q]): ZStream[Client & Producer, Throwable, Event]

object Crawler:

  val GraboidEventTopic = "graboid.event"

  def apply(dataCentre: DataCentre): RIO[Client, Crawler] =
    ZIO.succeed(wire[Impl])

  private class Impl(dataCentre: DataCentre) extends Crawler:

    override def apply[Q](query: Q)(using UpdateQueryParam[Q]): ZStream[Client & Producer, Throwable, Event] =
      val source = for
        url            <- ZIO.fromEither(URL.decode(dataCentre.url))
        newQueryParams <- ZIO.fromTry(summon[UpdateQueryParam[Q]](query, url.queryParams))
        response       <- HttpChecker[ExternalServiceException](
                            "An external error occurred!",
                            Client.request(Request.get(url.withQueryParams(newQueryParams)))
                          )
      yield response.body.asStream

      QuakeMLParser(ZStream.unwrap(source))
        .tap(event => ZIO.logDebug(s"Event ${event.publicId.resourceId} has been collected."))
        .mapZIO(publish)
        .tapErrorCause(ZIO.logErrorCause("An error occurred during crawling.", _))
        .mapError(GraboidException.CrawlingException("It was impossible to collect the events!", _)) @@ ZStreamAspect
        .annotated(
          "crawler.dataCentre.id" -> dataCentre.id
        )

    private def publish(event: Event): RIO[Producer, Event] =
      for
        bytes <- ZIO.attempt(Cbor.encode(event).toByteArray)
        _     <-
          Producer
            .produce(ProducerRecord(GraboidEventTopic, bytes), Serde.string, Serde.byteArray)
            .mapError(
              GraboidException.CrawlingException(s"It was impossible to publish event ${event.publicId.resourceId}!", _)
            )
        _     <- ZIO.logDebug(s"Event ${event.publicId.resourceId} was published.")
      yield event
