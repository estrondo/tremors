package graboid

import com.softwaremill.macwire.wire
import graboid.Crawler.Type
import graboid.fdsn.FDSNCrawler
import zio.Task
import zio.ULayer
import zio.ZIO

trait CrawlerFactory:

  def apply(publisher: Publisher, execution: CrawlerExecution): Task[Crawler]

object CrawlerFactory:

  def apply(
      httpLayer: ULayer[HttpService]
  ): CrawlerFactory =
    wire[Impl]

  private class Impl(
      httpLayer: ULayer[HttpService]
  ) extends CrawlerFactory:

    def apply(publisher: Publisher, execution: CrawlerExecution): Task[Crawler] = ZIO.attempt {
      publisher.`type` match
        case Type.FDSN => FDSNCrawler(httpLayer, publisher)
    } <* ZIO.logDebug(s"It's been created a Crawler from publisher=${publisher.key}.")
