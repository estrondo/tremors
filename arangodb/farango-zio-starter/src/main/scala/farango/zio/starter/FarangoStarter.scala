package farango.zio.starter

import com.arangodb.async.ArangoDBAsync
import com.arangodb.mapping.ArangoJack
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import farango.Database
import farango.DocumentCollection
import farango.zio.given
import zio.RIO
import zio.Schedule
import zio.Scheduler
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer
import zio.durationInt

object FarangoStarter:

  def apply(config: ArangoConfig): Task[Database] = ZIO.attempt {
    val arangoJack = ArangoJack()
    arangoJack.configure(mapper => mapper.registerModule(DefaultScalaModule))

    var arangoDB = ArangoDBAsync
      .Builder()
      .serializer(arangoJack)
      .user(config.username)
      .password(config.password)

    for host <- config.hosts do arangoDB.host(host.hostname, host.port)

    Database(arangoDB.build().db(config.database))
  }

  def layer(config: ArangoConfig): TaskLayer[Database] = ZLayer {
    for
      effect   <- apply(config).memoize
      database <- effect
    yield database
  }

  def getDocumentCollection(name: String, recurs: Int = 10, interval: Int = 3): RIO[Database, DocumentCollection] =
    (for
      database   <- ZIO.service[Database]
      collection <- database.documentCollection(name)
    yield collection).retry(Schedule.recurs(recurs) || Schedule.spaced(interval.seconds))
