package tremors.graboid.fdsn

import tremors.graboid.Crawler
import tremors.graboid.Crawler.Info
import tremors.graboid.fdsn.FDSNCrawler.Config
import zio.UIO
import zio.ZIO
import zio.stream.Stream
import zio.stream.ZStream

import java.net.URL
import zhttp.service.Client
import zio.TaskLayer
import zio.Task
import zio.ULayer
import zhttp.service.EventLoopGroup
import zhttp.service.ChannelFactory
import zhttp.http.Response
import tremors.graboid.GraboidException
import zio.Chunk
import tremors.graboid.HttpService
import scala.util.Success
import scala.util.Try
import scala.util.Failure

object FDSNCrawler:

  case class Config(
      organization: String,
      query: Option[URL]
  )

class FDSNCrawler(
    config: Config,
    httpService: ULayer[HttpService],
    parserFactory: QuakeMLParserFactory
) extends Crawler:

  override def crawl(): UIO[EventStream] =
    config.query match
      case Some(queryURL) => crawl(queryURL)
      case None           =>
        for _ <- ZIO.logInfo(s"There is no query in ${config.organization}.")
        yield ZStream.never

  private def crawl(queryURL: URL): UIO[EventStream] =
    val stream = for
      _        <- ZIO.logInfo(s"Fetching events from ${config.organization} @ ${queryURL}.")
      response <- HttpService
                    .get(queryURL)
                    .provideLayer(httpService)
      stream   <- converToEventStream(response)
    yield stream

    stream.catchAll { error =>
      ZIO.succeed(
        ZStream.fail(
          GraboidException.Unexpected(
            s"Impossible to fetch events from ${config.organization} @ ${queryURL}!",
            error
          )
        )
      )
    }

  private def converToEventStream(response: Response): Task[EventStream] =
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
                        case Success(None)  =>
                          ZIO.succeed((newChunk, None))
                        case Success(some)  =>
                          ZIO.succeed((Chunk.empty, some))
                        case Failure(cause) =>
                          ZIO.fail(GraboidException.CrawlerException("", cause))
                    }
                    .collect { case Some(info) => info }
                }
    yield stream
