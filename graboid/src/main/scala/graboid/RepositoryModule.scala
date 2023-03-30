package graboid

import com.softwaremill.macwire.wire
import com.softwaremill.tagging.@@
import com.softwaremill.tagging.given
import farango.DocumentCollection
import zio.Task
import zio.ZIO

trait RepositoryModule:

  def publisherRepository: PublisherRepository

  def crawlerExecutionRepository: CrawlerExecutionRepository

object RepositoryModule:

  val PublisherCollectionName        = "publisher"
  val CrawlerExecutionCollectionName = "crawler_execution"

  def apply(arangoModule: ArangoModule): Task[RepositoryModule] =
    for
      publisherCollection        <- arangoModule
                                      .getColl[ForPublisher](PublisherCollectionName)
      crawlerExecutionCollection <- arangoModule
                                      .getColl[ForCrawlerExecution](CrawlerExecutionCollectionName)
    yield wire[Impl]

  trait ForPublisher
  trait ForCrawlerExecution

  case class Coll(collection: DocumentCollection)

  extension (module: ArangoModule)
    private def getColl[T](name: String): Task[Coll @@ T] =
      module.getDocumentCollection(name).map(Coll.apply).taggedWithF[T]

  private class Impl(
      publisherColl: Coll @@ ForPublisher,
      crawlerExecutionColl: Coll @@ ForCrawlerExecution
  ) extends RepositoryModule:

    val publisherRepository = PublisherRepository(publisherColl.collection)

    val crawlerExecutionRepository = CrawlerExecutionRepository(crawlerExecutionColl.collection)
