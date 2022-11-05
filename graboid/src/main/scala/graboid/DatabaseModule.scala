package graboid

import graboid.repository.TimelineRepository

import zio.TaskLayer
import zio.{ZIO, Task}
import zio.ZLayer
import graboid.config.ArangoConfig
import graboid.config.GraboidConfig
import com.arangodb.ArangoDatabase
import scala.collection.immutable.HashMap
import farango.FarangoDatabase

trait DatabaseModule:

  val timelineRepositoryLayer: TaskLayer[TimelineRepository]

  val crawlerRepositoryLayer: TaskLayer[CrawlerRepository]

object DatabaseModule:

  def apply(config: GraboidConfig): Task[DatabaseModule] = ZIO.attempt {
    DatabaseModuleImpl(config)
  }

private[graboid] class DatabaseModuleImpl(config: GraboidConfig) extends DatabaseModule:

  val crawlerRepository = CrawlerRepository(createDatabase(config.crawlerRepository))

  val timelineRepository = TimelineRepository(createDatabase(config.timelineRepository))

  private def createDatabase(config: ArangoConfig): FarangoDatabase =
    FarangoDatabase(
      FarangoDatabase.Config(
        name = config.database,
        user = config.username,
        password = config.password,
        hosts = for host <- config.hosts yield (host.hostname, host.port)
      )
    )

  override val crawlerRepositoryLayer: TaskLayer[CrawlerRepository] =
    ZLayer.succeed(crawlerRepository)

  val timelineRepositoryLayer: TaskLayer[TimelineRepository] =
    ZLayer.succeed(timelineRepository)
