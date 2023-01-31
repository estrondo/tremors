package graboid

import com.softwaremill.macwire.wire
import com.softwaremill.macwire.wireWith
import com.softwaremill.tagging.@@
import com.softwaremill.tagging.given
import farango.DocumentCollection
import graboid.config.GraboidConfig
import zio.Task
import zio.ZIO

trait CoreModule:

  val publisherManager: PublisherManager

  val eventManager: EventManager

  val crawlerExecutor: CrawlerExecutor

object CoreModule:

  trait ForPublisher
  trait ForCrawlerExecution

  case class CollectionWrapper(collection: DocumentCollection)

  val PublisherCollectionName        = "publisher"
  val CrawlerExecutionCollectionName = "crawler_execution"

  def apply(
      config: GraboidConfig,
      arangoModule: ArangoModule,
      kafkaModule: KafkaModule,
      httpModule: HttpModule
  ): Task[CoreModule] =
    for
      publisherCollection        <- arangoModule
                                      .getDocumentCollection(PublisherCollectionName)
                                      .map(CollectionWrapper.apply)
                                      .taggedWithF[ForPublisher]
      crawlerExecutionCollection <- arangoModule
                                      .getDocumentCollection(CrawlerExecutionCollectionName)
                                      .map(CollectionWrapper.apply)
                                      .taggedWithF[ForCrawlerExecution]
    yield wire[Impl]

  private class Impl(
      config: GraboidConfig,
      publisherCollection: CollectionWrapper @@ ForPublisher,
      crawlerExecutionCollection: CollectionWrapper @@ ForCrawlerExecution,
      kafkaModule: KafkaModule,
      httpModule: HttpModule
  ) extends CoreModule:

    val publisherRepository = PublisherRepository(publisherCollection.collection)

    val crawlerExecutionRepository = CrawlerExecutionRepository(crawlerExecutionCollection.collection)

    val publisherValidator: PublisherManager.Validator = ZIO.succeed(_)

    override val publisherManager: PublisherManager = wireWith(PublisherManager.apply)

    override val eventManager: EventManager = EventManager(kafkaModule.kafkaManager.producerLayer)

    val scheduler: CrawlerScheduler = CrawlerScheduler()

    val crawlerFactory: CrawlerFactory = CrawlerFactory(httpModule.serviceLayer)

    override val crawlerExecutor: CrawlerExecutor = CrawlerExecutor(
      crawlerExecutionRepository,
      scheduler,
      publisherManager,
      eventManager,
      crawlerFactory
    )
