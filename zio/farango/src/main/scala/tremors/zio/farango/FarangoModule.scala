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
import zio.ZIOAspect
import zio.durationInt

trait FarangoModule:

  def collection(
      name: String,
      indexes: Seq[IndexDescription] = Nil,
      options: CollectionCreateOptions = CollectionCreateOptions()
  ): Task[Collection]

object FarangoModule:

  private val DefaultPort = 8529

  def apply(arangoConfig: ArangoConfig): Task[FarangoModule] =
    for
      farangoConfig <- createFarangoConfig(arangoConfig)
      db            <- ZIO.fromTry(SyncDB(farangoConfig))
      database      <- prepDatabase(db.database(arangoConfig.database), arangoConfig)
    yield Default(db, database)

  private def prepDatabase(database: SyncDatabase, config: ArangoConfig): Task[SyncDatabase] =
    (for
      exists <- database.exists
      _      <- if exists then ZIO.unit else ZIO.logDebug(s"Creating database!") *> database.create()
    yield database)
      .tapErrorCause(cause =>
        ZIO.logWarning(s"It was impossible to create database.") *>
          ZIO.logDebugCause(s"An error occurred during attempt to create a database!", cause)
      )
      .retry(Schedule.forever && Schedule.spaced(5.seconds)) @@ ZIOAspect.annotated(
      "farangoModule.database" -> database.name,
      "farangoModule.hosts"    -> config.hosts,
      "farangoModule.username" -> config.username
    )

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
                    for
                      _ <- ZIO.logDebug(s"Creating the collection ${name}.")
                      _ <- collection.create().tapErrorCause { cause =>
                             for
                               _ <- ZIO.logWarning(s"It was impossible to create the collection ${name}.")
                               _ <-
                                 ZIO
                                   .logDebugCause(s"An error occurred during attempt to create the collection $name!", cause)
                             yield ()
                           }
                    yield ()
      yield collection)
        .retry(Schedule.forever && Schedule.spaced(5.seconds))
