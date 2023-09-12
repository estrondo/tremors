package graboid.crawling

import graboid.DataCentre
import graboid.GraboidException
import graboid.http.UpdateQueryParam
import graboid.quakeml.parser.QuakeMLParser
import io.bullet.borer.Cbor
import org.apache.kafka.clients.producer.ProducerRecord
import tremors.ExternalServiceException
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.quakeml.Event
import tremors.zio.http.HttpChecker
import zio.RIO
import zio.ZIO
import zio.http.Client
import zio.http.QueryParams
import zio.http.Request
import zio.http.URL
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.stream.ZStream

trait EventCrawler:

  def apply(query: EventCrawlingQuery): ZStream[Client & Producer, Throwable, EventCrawler.FoundEvent]

object EventCrawler:

  val GraboidEventTopic = "graboid.event"

  def apply(dataCentre: DataCentre, keyGenerator: KeyGenerator): RIO[Client, EventCrawler] =
    dataCentre.event match
      case Some(event) =>
        for url <- ZIO
                     .fromEither(URL.decode(event))
                     .mapError(
                       GraboidException.CrawlingException(s"Invalid event URL for DataCentre ${dataCentre.id}!", _)
                     )
        yield Impl(dataCentre, url, keyGenerator)

      case None =>
        ZIO.fail(GraboidException.CrawlingException(s"DataCentre ${dataCentre.id} has no event URL!"))

  trait Factory:
    def apply(dataCentre: DataCentre): RIO[Client, EventCrawler]

  case class FoundEvent(dataCentre: DataCentre, event: Event)

  private class Impl(dataCentre: DataCentre, event: URL, keyGenerator: KeyGenerator) extends EventCrawler:

    override def apply(query: EventCrawlingQuery): ZStream[Client & Producer, Throwable, FoundEvent] =

      def performQuery(query: QueryParams) =
        ZStream
          .fromZIO(
            HttpChecker[ExternalServiceException](
              "Unexpected FDSN Event Service Response!",
              Client.request(Request.get(event.withQueryParams(query)))
            )
          )
          .flatMap(response => QuakeMLParser(response.body.asStream).map(FoundEvent(dataCentre, _)))
          .mapZIO(publishIt)

      ZStream
        .fromIterableZIO(UpdateQueryParam(query, event.queryParams))
        .flatMap(performQuery)

    private def publishIt(foundEvent: FoundEvent): RIO[Producer & Client, FoundEvent] =
      for
        content  <- ZIO.attempt(Cbor.encode(foundEvent.event).toByteArray)
        record    = ProducerRecord(GraboidEventTopic, keyGenerator.generate(KeyLength.Medium), content)
        metadata <- Producer.produce(record, Serde.string, Serde.byteArray)
        _        <- ZIO.logDebug(
                      s"An event id=${foundEvent.event.publicId.resourceId} has been published: offset=${metadata.offset()}"
                    )
      yield foundEvent
