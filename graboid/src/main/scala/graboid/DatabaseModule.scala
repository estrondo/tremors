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
import zio.Schedule
import zio.durationInt
import zio.Cause

trait DatabaseModule:

  val timelineRepositoryLayer: TaskLayer[TimelineRepository]

  val crawlerRepositoryLayer: TaskLayer[CrawlerRepository]

object DatabaseModule:

  def apply(config: GraboidConfig): Task[DatabaseModule] =
    val schedule = Schedule.recurs(10) && Schedule.spaced(3.seconds)

    def create[T](
        name: String,
        database: FarangoDatabase,
        factory: FarangoDatabase => Task[T]
    ) =
      (ZIO.logInfo(s"Attempt to create $name.") *> factory(database))
        .retry(schedule)

    for
      crawlerDatabase   <- createDatabase(config.crawlerRepository)
      timelineDatabase  <- createDatabase(config.timelineRepository)
      crawlerRepository <- create("CrawlerRepository", crawlerDatabase, CrawlerRepository(_))
      timeleRepository  <- create("TimelineRepository", timelineDatabase, TimelineRepository(_))
    yield wire[DatabaseModuleImpl]

  private def createDatabase(config: ArangoConfig): Task[FarangoDatabase] =

    def connect() = FarangoDatabase(
      FarangoDatabase.Config(
        name = config.database,
        user = config.username,
        password = config.password,
        hosts = for host <- config.hosts yield (host.hostname, host.port)
      )
    )

    for
      _        <- ZIO.logInfo(s"Attempt to connect to ArangoDB: ${config.database}")
      database <- ZIO.attemptBlocking(connect())
    yield database

private[graboid] class DatabaseModuleImpl(
    val crawlerRepository: CrawlerRepository,
    val timelineRepository: TimelineRepository
) extends DatabaseModule:

  override val crawlerRepositoryLayer: TaskLayer[CrawlerRepository] =
    ZLayer.succeed(crawlerRepository)

  override val timelineRepositoryLayer: TaskLayer[TimelineRepository] =
    ZLayer.succeed(timelineRepository)
