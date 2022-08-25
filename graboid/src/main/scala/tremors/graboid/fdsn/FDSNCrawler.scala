package tremors.graboid.fdsn

import io.lemonlabs.uri.Url
import io.lemonlabs.uri.config.{ExcludeNones, UriConfig}
import tremors.graboid.{>>>, Crawler, CrawlerTimeline, GraboidException, HttpService, given}
import tremors.graboid.Crawler.Info
import tremors.graboid.fdsn.FDSNCrawler.Config
import tremors.graboid.UrlTypesafe.given
import zhttp.http.Response
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.{Cause, Chunk, Task, TaskLayer, UIO, ULayer, ZIO}
import zio.stream.{Stream, ZStream}

import java.net.URL
import java.time.ZonedDateTime
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

object FDSNCrawler:

  case class Config(
      organization: String,
      query: Option[URL]
  )

  given UriConfig = UriConfig(
    renderQuery = ExcludeNones
  )

class FDSNCrawler(
    config: Config,
    httpService: ULayer[HttpService],
    timeline: CrawlerTimeline,
    parserFactory: QuakeMLParserFactory
) extends Crawler:

  import FDSNCrawler.given_UriConfig

  override def crawl(): UIO[EventStream] =
    config.query match
      case Some(url) => crawl(url)
      case None      =>
        ZIO.logInfo(s"There is no query in ${config.organization}.") >>> ZIO.succeed(ZStream.never)

  private def crawl(queryURL: URL): UIO[EventStream] =
    val stream = for
      url      <- parseUrl(queryURL)
      _        <- ZIO.logInfo(s"Fetching events from ${config.organization} @ $url.")
      response <- HttpService
                    .get(queryURL)
                    .provideLayer(httpService)
      stream   <- readStream(response)
    yield stream

    stream.catchAll { error =>
      ZIO.succeed(
        ZStream.fail(
          GraboidException.Unexpected(
            s"Impossible to fetch events from ${config.organization} @ $queryURL!",
            error
          )
        )
      )
    }

  private def endtime: Task[Option[ZonedDateTime]] = ZIO.none

  private def includeParams(url: Url): Task[Url] =
    for
      starttime <- this.starttime
      endtime   <- this.endtime
    yield url
      .addParam("starttime" -> starttime)
      .addParam("endtime" -> endtime)
      .addParam("includeallorigins" -> Some(false))
      .addParam("format" -> Some("xml"))

  private def parseUrl(originalURL: URL): Task[Url] =
    for
      url       <- ZIO.succeed(Url.parse(originalURL))
      parsedUrl <- includeParams(url)
    yield parsedUrl

  private def readStream(response: Response): Task[EventStream] =
    for
      parser <- ZIO
                  .fromTry(Try(parserFactory()))
                  .catchAll(cause =>
                    ZIO.fail(GraboidException.Unexpected("Unexpected parser exception!", cause))
                  )
      stream <- ZIO.succeed {
                  response.bodyAsStream
                    .mapAccumZIO(Chunk.empty[Byte]) { (chunk, byte) =>
                      val newChunk = chunk :+ byte
                      Try(parser.evaluate(newChunk)) match
                        case Success(None) =>
                          ZIO.succeed((newChunk, None))

                        case Success(some) =>
                          ZIO.succeed((Chunk.empty, some))

                        case Failure(cause) =>
                          ZIO.fail(GraboidException.CrawlerException("", cause))
                    }
                    .collect { case Some(info) => info }
                }
    yield stream

  private def starttime: Task[Option[ZonedDateTime]] = timeline.lastUpdate
