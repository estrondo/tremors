package toph.module

import com.softwaremill.macwire.wire
import farango.Database
import farango.zio.starter.ArangoConfig
import farango.zio.starter.FarangoStarter
import zio.Task
import zio.ZIO
import farango.DocumentCollection
import zio.ZLayer

trait FarangoModule:

  def collection(name: String): Task[DocumentCollection]

object FarangoModule:

  def apply(config: ArangoConfig): Task[FarangoModule] =
    for database <- FarangoStarter(config)
    yield wire[Impl]

  private class Impl(database: Database) extends FarangoModule:

    override def collection(name: String): Task[DocumentCollection] =
      FarangoStarter
        .getDocumentCollection(name)
        .provideLayer(ZLayer.succeed(database))
