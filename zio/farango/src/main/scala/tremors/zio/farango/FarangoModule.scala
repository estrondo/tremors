package tremors.zio.farango

import com.arangodb.ArangoDBException
import com.arangodb.model.CollectionCreateOptions
import com.bedatadriven.jackson.datatype.jts.JtsModule
import one.estrondo.farango.Config
import one.estrondo.farango.IndexDescription
import one.estrondo.farango.JacksonConsumer
import one.estrondo.farango.sync.SyncDatabase
import one.estrondo.farango.sync.SyncDB
import org.locationtech.jts.geom.GeometryFactory
import zio.Schedule
import zio.Task
import zio.ZIO
import zio.durationInt

trait FarangoModule:

  def collection(
      name: String,
      indexes: Seq[IndexDescription] = Nil,
      options: CollectionCreateOptions = CollectionCreateOptions(),
  ): Task[CollectionManager]

object FarangoModule:

  private val DefaultPort = 8529

  def apply(arangoConfig: ArangoConfig, geometryFactory: GeometryFactory): Task[FarangoModule] =
    for
      farangoConfig <- createFarangoConfig(arangoConfig, geometryFactory)
      db            <- ZIO.fromTry(SyncDB(farangoConfig))
    yield Default(db, db.database(arangoConfig.database))

  private def createFarangoConfig(config: ArangoConfig, geometryFactory: GeometryFactory): Task[Config] = ZIO.attempt {
    val regex = """([^:]+)(:(\d+))?""".r

    config.hosts
      .split("""\s*,\S*""")
      .foldLeft(Config()) { (config, host) =>
        host match
          case regex(hostname, _, port) => config.addHost(hostname, port.toInt)
          case regex(hostname)          => config.addHost(hostname, DefaultPort)
          case _                        => throw ArangoDBException(s"ZIO Farango: Invalid host configuration: $host!")
      }
      .withUser(config.username)
      .withPassword(config.password)
      .withRootPassword(config.rootPassword)
      .withSerde(JacksonConsumer(_.registerModule(JtsModule(geometryFactory))))
  }

  private class Default(db: SyncDB, database: SyncDatabase) extends FarangoModule:

    override def collection(
        name: String,
        indexes: Seq[IndexDescription] = Nil,
        options: CollectionCreateOptions = CollectionCreateOptions().waitForSync(true),
    ): Task[CollectionManager] = ZIO.succeed {
      CollectionManager(database.collection(name, indexes, options), database, Schedule.spaced(5.seconds))
    }
