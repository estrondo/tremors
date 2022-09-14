package tremors.graboid.fdsn

import io.lemonlabs.uri.Url
import io.lemonlabs.uri.config.ExcludeNones
import io.lemonlabs.uri.config.UriConfig
import tremors.graboid.Crawler
import tremors.graboid.Crawler.Info
import tremors.graboid.CrawlerTimeline
import tremors.graboid.GraboidException
import tremors.graboid.HttpService
import tremors.graboid.UrlTypesafe.given
import tremors.graboid.fdsn.FDSNCrawler.Config
import tremors.graboid.given
import tremors.graboid.quakeml.QuakeMLParser
import zhttp.http.Response
import zhttp.http.Status.Ok
import zhttp.service.ChannelFactory
import zhttp.service.Client
import zhttp.service.EventLoopGroup
import zio.Cause
import zio.Chunk
import zio.Task
import zio.TaskLayer
import zio.UIO
import zio.ULayer
import zio.ZIO
import zio.stream.Stream
import zio.stream.ZStream

import java.net.URL
import java.time.ZonedDateTime
import scala.language.implicitConversions
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object FDSNCrawler:

  case class Config(
      organization: String,
      queryURL: URL
  )

class FDSNCrawler(
    config: Config,
    httpServiceLayer: ULayer[HttpService],
    timeline: CrawlerTimeline,
    parser: QuakeMLParser
) extends Crawler:

  override def crawl(): Task[Crawler.Stream] =
    (
      for
        url      <- parseUrl(config.queryURL)
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

  private def includeParams(url: Url): Task[Url] =
    for nextInterval <- this.nextInterval
    yield
      val (starttime, endtime) = nextInterval
      url
        .addParam("starttime" -> starttime)
        .addParam("endtime" -> endtime)
        .addParam("includeallorigins" -> Some(false))
        .addParam("format" -> Some("xml"))

  private def nextInterval: UIO[(Option[ZonedDateTime], Option[ZonedDateTime])] =
    ZIO.succeed((None, None))

  private def parseUrl(originalURL: URL): Task[Url] =
    given UriConfig = UriConfig(
      renderQuery = ExcludeNones
    )

    for
      url       <- ZIO.attempt(Url.parse(originalURL.toString()))
      parsedUrl <- includeParams(url)
    yield parsedUrl

  private def readStream(response: Response): Task[Crawler.Stream] =
    for
      bodyStream <- extractBodyStream(response)
      stream     <- parser.parse(bodyStream)
    yield stream

  private def extractBodyStream(response: Response): Task[ZStream[Any, Throwable, Byte]] =
    def fail(fn: String => GraboidException): Task[ZStream[Any, Throwable, Byte]] =
      response.bodyAsString.flatMap { body =>
        ZIO.fail(fn(body))
      }

    response.status match
      case s if s.isSuccess => ZIO.succeed(response.bodyAsStream)

      case s if s.isClientError =>
        fail(body => GraboidException.IllegalRequest(s"$s: $body"))

      case s if s.isServerError =>
        fail(body => GraboidException.Unexpected(s"Server response ($s): $body"))

      case s if s.isRedirection =>
        ZIO.fail(GraboidException.IllegalRequest(s"Redirect was rejected: ${response.location}."))

      case s => ZIO.fail(GraboidException.Unexpected(s"Unexpected status $s!"))
