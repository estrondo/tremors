package toph.module

import com.bedatadriven.jackson.datatype.jts.JtsModule
import com.softwaremill.macwire.wire
import farango.Database
import farango.DocumentCollection
import farango.zio.starter.ArangoConfig
import farango.zio.starter.FarangoStarter
import toph.geom.Factory
import zio.Task
import zio.ZIO
import zio.ZLayer

trait FarangoModule:

  def collection(name: String): Task[DocumentCollection]

object FarangoModule:

  def apply(config: ArangoConfig): Task[FarangoModule] =
    for database <- FarangoStarter(config, Some({ mapper => mapper.registerModule(new JtsModule(Factory)) }))
    yield wire[Impl]

  private class Impl(database: Database) extends FarangoModule:

    override def collection(name: String): Task[DocumentCollection] =
      FarangoStarter
        .getDocumentCollection(name)
        .provideLayer(ZLayer.succeed(database))
