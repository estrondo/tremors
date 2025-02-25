package graboid.crawling

import graboid.DataCentre
import graboid.GraboidException
import graboid.http.HttpClient
import graboid.http.UpdateQueryParam
import graboid.quakeml.parser.QuakeMLParser
import io.bullet.borer.Cbor
import java.time.Duration
import org.apache.kafka.clients.producer.ProducerRecord
import tremors.ExternalServiceException
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.quakeml.Event
import tremors.zio.http.HttpChecker
import zio.Cause
import zio.RIO
import zio.Schedule
import zio.Scope
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
                       GraboidException.Crawling(s"Invalid event URL for DataCentre ${dataCentre.id}!", _),
                     )
        yield Impl(dataCentre, url, keyGenerator)

      case None =>
        ZIO.fail(GraboidException.Crawling(s"DataCentre ${dataCentre.id} has no event URL!"))

  trait Factory:
    def apply(dataCentre: DataCentre): RIO[Client, EventCrawler]

  case class FoundEvent(dataCentre: DataCentre, event: Event)

  private class Impl(dataCentre: DataCentre, event: URL, keyGenerator: KeyGenerator) extends EventCrawler:

    override def apply(query: EventCrawlingQuery): ZStream[Client & Producer, Throwable, FoundEvent] =

      def performQuery(query: QueryParams) =
        val request = Request.get(event.addQueryParams(query))
        ZStream
          .fromZIO {
            val params           = query.map.view.map((k, v) => s"$k=${v.head}").mkString(", ")
            val logFailedAttempt =
              ZIO.logWarning(s"Crawling has failed, it will be attempt soon.") as true

            (ZIO.logDebug(s"Crawling: $params.") *> HttpChecker[ExternalServiceException](
              "Unexpected FDSN Event Service Response!",
              HttpClient
                .request(request)
                .timeoutFail(GraboidException.Crawling("Request timeout reached."))(Duration.ofSeconds(8)),
            )).retry(
              Schedule.recurs(6) &&
                Schedule.fibonacci(Duration.ofMillis(1000)) &&
                Schedule.recurWhileZIO(_ => logFailedAttempt),
            )
          }
          .flatMap(response =>
            QuakeMLParser
              .parse(
                response.body.asStream
                  .grouped(8 * 1024)
                  .timeoutFail(GraboidException.Crawling("Streaming timeout reached."))(Duration.ofSeconds(8)),
              )
              .mapZIO(publishIt),
          )

      ZStream
        .fromIterableZIO(UpdateQueryParam(query, event.queryParams))
        .flatMap(performQuery)
        .provideSomeLayer(Scope.default)

    private def publishIt(event: Event): RIO[Producer, FoundEvent] =
      for
        content  <- ZIO.attempt(Cbor.encode(event).toByteArray)
        record    = ProducerRecord(GraboidEventTopic, keyGenerator.generate(KeyLength.Medium), content)
        metadata <- Producer.produce(record, Serde.string, Serde.byteArray)
        _        <- ZIO.logDebug(
                      s"An event id=${event.publicId.resourceId} has been published: offset=${metadata.offset()}",
                    )
      yield FoundEvent(dataCentre, event)

  object Factory extends Factory:

    override def apply(dataCentre: DataCentre): RIO[Client, EventCrawler] =
      EventCrawler(dataCentre, KeyGenerator)
