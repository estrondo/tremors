package graboid

import com.softwaremill.macwire.wire
import farango.FarangoDatabase
import farango.FarangoDocumentCollection
import graboid.config.ArangoConfig
import zio.Schedule
import zio.Task
import zio.ZIO
import zio.given
import ziorango.given

trait ArangoModule:

  def getDocumentCollection(name: String): Task[FarangoDocumentCollection]

object ArangoModule:

  def apply(config: ArangoConfig): Task[ArangoModule] = ZIO.attempt {
    wire[ArangoModuleImpl]
  }

  private class ArangoModuleImpl(config: ArangoConfig) extends ArangoModule:

    val retryPolicy = Schedule.recurs(30) || Schedule.spaced(2.seconds)

    val getDatabase =
      val memoized = ZIO.attempt {
        FarangoDatabase(
          FarangoDatabase.Config(
            name = config.database,
            user = config.username,
            password = config.password,
            hosts = config.hosts.map(x => (x.hostname, x.port))
          )
        )
      }.memoize

      for
        effect   <- memoized
        database <- effect
      yield database

    override def getDocumentCollection(name: String): Task[FarangoDocumentCollection] =
      (for
        database   <- getDatabase
        collection <- database.documentCollection(name)
      yield collection).retry(retryPolicy)
