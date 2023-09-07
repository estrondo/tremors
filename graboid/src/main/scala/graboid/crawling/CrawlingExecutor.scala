package graboid.crawling

import com.softwaremill.macwire.wire
import graboid.manager.DataCentreManager
import graboid.repository.CrawlingExecutionRepository
import graboid.time.ZonedDateTimeService
import tremors.generator.KeyGenerator
import zio.http.Client
import zio.kafka.producer.Producer
import zio.stream.ZStream

trait CrawlingExecutor:

  def execute(
      query: EventCrawlingQuery
  ): ZStream[ZonedDateTimeService & Client & Producer, Throwable, EventCrawler.FoundEvent]

object CrawlingExecutor:

  def apply(
      repository: CrawlingExecutionRepository,
      dataCentreManager: DataCentreManager,
      eventCrawlerFactory: EventCrawler.Factory,
      keyGenerator: KeyGenerator
  ): CrawlingExecutor =
    wire[Impl]

  private class Impl(
      repository: CrawlingExecutionRepository,
      dataCentreManager: DataCentreManager,
      eventCrawlerFactory: EventCrawler.Factory,
      keyGenerator: KeyGenerator
  ) extends CrawlingExecutor:

    override def execute(
        query: EventCrawlingQuery
    ): ZStream[ZonedDateTimeService & Client & Producer, Throwable, EventCrawler.FoundEvent] = ???
