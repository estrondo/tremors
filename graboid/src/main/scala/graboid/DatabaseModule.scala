package graboid

import com.arangodb.ArangoDatabase
import farango.FarangoDatabase
import graboid.config.ArangoConfig
import graboid.config.GraboidConfig
import graboid.repository.TimelineRepository
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer
import com.softwaremill.macwire.wire

import scala.collection.immutable.HashMap
import farango.FarangoDocumentCollection

trait DatabaseModule:

  val timelineRepositoryLayer: TaskLayer[TimelineRepository]

  val crawlerRepositoryLayer: TaskLayer[CrawlerRepository]

object DatabaseModule:

  def apply(config: GraboidConfig): Task[DatabaseModule] =
    for crawlerDatabase <- ZIO.succeed(createDatabase(config.crawlerRepository))
    yield ???

  private def createDatabase(config: ArangoConfig): FarangoDatabase =
    FarangoDatabase(
      FarangoDatabase.Config(
        name = config.database,
        user = config.username,
        password = config.password,
        hosts = for host <- config.hosts yield (host.hostname, host.port)
      )
    )

private[graboid] class DatabaseModuleImpl(
    graboid: FarangoDatabase,
    crawlerCollection: FarangoDocumentCollection,
    timelineCollection: FarangoDocumentCollection
) extends DatabaseModule:

  val crawlerRepository = CrawlerRepository(crawlerCollection)

  val timelineRepository = TimelineRepository(timelineCollection)

  override val crawlerRepositoryLayer: TaskLayer[CrawlerRepository] =
    ZLayer.succeed(crawlerRepository)

  val timelineRepositoryLayer: TaskLayer[TimelineRepository] =
    ZLayer.succeed(timelineRepository)
