package graboid

import com.softwaremill.macwire.wire
import farango.Database
import farango.DocumentCollection
import graboid.config.ArangoConfig
import zio.Schedule
import zio.Task
import zio.ZIO
import zio.given
import farango.zio.given
import com.arangodb.async.ArangoDBAsync
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.arangodb.mapping.ArangoJack

trait ArangoModule:

  def getDocumentCollection(name: String): Task[DocumentCollection]

object ArangoModule:

  def apply(config: ArangoConfig): Task[ArangoModule] = ZIO.attempt {
    wire[ArangoModuleImpl]
  }

  private class ArangoModuleImpl(config: ArangoConfig) extends ArangoModule:

    val retryPolicy = Schedule.recurs(30) || Schedule.spaced(2.seconds)

    val getDatabase =
      val memoized = ZIO.attempt {
        var arangoDB = ArangoDBAsync
          .Builder()
          .serializer(ArangoJack())
          .user(config.username)
          .password(config.password)

        for host <- config.hosts do arangoDB.host(host.hostname, host.port)

        Database(arangoDB.build().db(config.database))
      }.memoize

      for
        effect   <- memoized
        database <- effect
      yield database

    override def getDocumentCollection(name: String): Task[DocumentCollection] =
      (for
        database   <- getDatabase
        collection <- database.documentCollection(name)
      yield collection).retry(retryPolicy)
