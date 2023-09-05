package graboid

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

trait EventCrawler:

  def apply[Q](query: Q)(using UpdateQueryParam[Q]): ZStream[Client & Producer, Throwable, EventCrawler.Result]

object EventCrawler:

  val GraboidEventTopic = "graboid.event"

  def apply(dataCentre: DataCentre): RIO[Client, EventCrawler] =
    dataCentre.event match
      case Some(event) => ZIO.succeed(Impl(dataCentre, event))
      case None        => ZIO.fail(GraboidException.CrawlingException(s"DataCentre ${dataCentre.id} has no event URL!"))

  sealed trait Result

  trait Factory:
    def apply(dataCentre: DataCentre): RIO[Client, EventCrawler]

  case class Success(event: Event) extends Result

  case class Failed(event: Event, cause: Throwable) extends Result

  private class Impl(dataCentre: DataCentre, event: String) extends EventCrawler:

    override def apply[Q](query: Q)(using UpdateQueryParam[Q]): ZStream[Client & Producer, Throwable, Result] =
      val source = for
        url            <- ZIO.fromEither(URL.decode(event))
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

    private def publish(event: Event): RIO[Producer, Result] =
      (for
        bytes <- ZIO.attempt(Cbor.encode(event).toByteArray)
        _     <- Producer.produce(ProducerRecord(GraboidEventTopic, bytes), Serde.string, Serde.byteArray)
        _     <- ZIO.logDebug(s"Event ${event.publicId.resourceId} was published.")
      yield Success(event))
        .catchAll(cause => ZIO.succeed(Failed(event, cause)))
