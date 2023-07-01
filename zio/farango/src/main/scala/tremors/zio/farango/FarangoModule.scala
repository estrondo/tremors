package tremors.zio.farango

import com.arangodb.ArangoDBException
import com.arangodb.model.CollectionCreateOptions
import one.estrondo.farango.Collection
import one.estrondo.farango.Config
import one.estrondo.farango.IndexDescription
import one.estrondo.farango.sync.SyncDatabase
import one.estrondo.farango.sync.SyncDB
import one.estrondo.farango.zio.given
import zio.Schedule
import zio.Task
import zio.ZIO
import zio.durationInt

trait FarangoModule:

  def collection(
      name: String,
      indexes: Seq[IndexDescription] = Nil,
      options: CollectionCreateOptions = CollectionCreateOptions()
  ): Task[Collection]

object FarangoModule:

  val DefaultPort = 8529

  def apply(arangoConfig: ArangoConfig): Task[FarangoModule] =
    for
      farangoConfig <- createFarangoConfig(arangoConfig)
      db            <- ZIO.fromTry(SyncDB(farangoConfig))
      database      <- prepDatabase(db.database(arangoConfig.database))
    yield Default(db, database)

  private def prepDatabase(database: SyncDatabase): Task[SyncDatabase] =
    (for
      exists <- ZIO.fromTry(database.root).flatMap(root => ZIO.attemptBlocking(root.exists()))
      _      <- if exists then ZIO.unit else ZIO.logDebug(s"Creating the database ${database.name}!") *> database.create()
    yield database)
      .tapErrorCause(cause =>
        ZIO.logWarning(s"It was impossible to create the database ${database.name}!") *> ZIO.logDebugCause(
          s"An error occurred during attempt to create the database ${database.name}!",
          cause
        )
      )
      .retry(Schedule.forever && Schedule.spaced(5.seconds))

  private def createFarangoConfig(config: ArangoConfig): Task[Config] = ZIO.attempt {
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
  }

  private class Default(db: SyncDB, database: SyncDatabase) extends FarangoModule:

    override def collection(
        name: String,
        indexes: Seq[IndexDescription] = Nil,
        options: CollectionCreateOptions = CollectionCreateOptions()
    ): Task[Collection] =
      val collection = database.collection(name, indexes, options)
      (for
        exists <- ZIO.attemptBlocking(collection.arango.exists())
        _      <- if exists then ZIO.unit
                  else
                    ZIO.logDebug(s"Creating the collection ${name}.") *> collection.create().tapErrorCause { cause =>
                      ZIO.logWarning(s"It was impossible to create the collection ${name}.") *> ZIO
                        .logDebugCause(s"An error occurred during attempt to create the collection $name!", cause)
                    }
      yield collection)
        .retry(Schedule.forever && Schedule.spaced(5.seconds))
