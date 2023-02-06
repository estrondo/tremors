package graboid

import com.arangodb.async.ArangoDBAsync
import com.arangodb.mapping.ArangoJack
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.softwaremill.macwire.wire
import farango.Database
import farango.DocumentCollection
import farango.zio.given
import farango.zio.starter.ArangoConfig
import farango.zio.starter.FarangoStarter
import zio.Schedule
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.given

trait ArangoModule:

  def getDocumentCollection(name: String): Task[DocumentCollection]

object ArangoModule:

  def apply(config: ArangoConfig): Task[ArangoModule] = ZIO.attempt {
    wire[Impl]
  }

  private class Impl(config: ArangoConfig) extends ArangoModule:

    private val DatabaseLayer = FarangoStarter.layer(config)

    override def getDocumentCollection(name: String): Task[DocumentCollection] =
      FarangoStarter.getDocumentCollection(name).provideLayer(DatabaseLayer)
