package graboid

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.zio.starter.ArangoConfig
import farango.zio.starter.FarangoStarter
import zio.Task
import zio.ZIO

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
