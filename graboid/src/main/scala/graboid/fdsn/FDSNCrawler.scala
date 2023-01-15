package graboid.fdsn

import graboid.Crawler
import graboid.GraboidException
import graboid.HttpService
import graboid.TimeWindow
import graboid.UrlTypesafe.given
import graboid.fdsn.FDSNCrawler.Config
import graboid.quakeml.QuakeMLParser
import io.lemonlabs.uri.Url
import io.lemonlabs.uri.config.ExcludeNones
import io.lemonlabs.uri.config.UriConfig
import zhttp.http.Response
import zio.Task
import zio.ULayer
import zio.ZIO
import zio.stream.ZStream

import java.net.URI
import java.net.URL
import graboid.EventPublisher

object FDSNCrawler:

  val TypeName = "fdsn"

  case class Config(
      organization: String,
      queryURL: URL
  )

  def apply(httpService: ULayer[HttpService])(publisher: EventPublisher): FDSNCrawler =
    new FDSNCrawler(
      Config(publisher.name, publisher.url),
      httpService,
      QuakeMLParser()
    )

class FDSNCrawler(
    config: Config,
    httpServiceLayer: ULayer[HttpService],
    parser: QuakeMLParser
) extends Crawler:

  override def crawl(window: TimeWindow): Task[Crawler.Stream] =
    (
      for
        url      <- parseUrl(config.queryURL, window)
        _        <- ZIO.logInfo(s"Fetching events from ${config.organization} @ $url.")
        response <- HttpService
                      .get(url.toString)
                      .provideLayer(httpServiceLayer)
        stream   <- readStream(response)
      yield stream
    ).mapError(
      GraboidException.Unexpected(
        s"Impossible to fetch events from ${config.organization} @ ${config.queryURL}!",
        _
      )
    )

  private def addParams(url: Url, window: TimeWindow): Task[Url] = ZIO.succeed {
    url
      .addParam("starttime" -> window.beginning)
      .addParam("endtime" -> window.ending)
      .addParam("includeallorigins" -> Some(false))
      .addParam("format" -> Some("xml"))
  }

  private def parseUrl(originalURL: URL, window: TimeWindow): Task[Url] =
    given UriConfig = UriConfig(
      renderQuery = ExcludeNones
    )

    for
      url       <- ZIO.attempt(Url.parse(originalURL.toString()))
      parsedUrl <- addParams(url, window)
    yield parsedUrl

  private def readStream(response: Response): Task[Crawler.Stream] =
    for
      bodyStream <- extractBodyStream(response)
      stream     <- parser.parse(bodyStream)
    yield stream

  private def extractBodyStream(response: Response): Task[ZStream[Any, Throwable, Byte]] =
    def fail(fn: String => GraboidException): Task[ZStream[Any, Throwable, Byte]] =
      response.body.asString.flatMap { body =>
        ZIO.fail(fn(body))
      }

    response.status match
      case s if s.isSuccess => ZIO.succeed(response.body.asStream)

      case s if s.isClientError =>
        fail(body => GraboidException.IllegalRequest(s"$s: $body"))

      case s if s.isServerError =>
        fail(body => GraboidException.Unexpected(s"Server response ($s): $body"))

      case s if s.isRedirection =>
        ZIO.fail(GraboidException.IllegalRequest(s"Redirect was rejected: ${response.location}."))

      case s => ZIO.fail(GraboidException.Unexpected(s"Unexpected status $s!"))
