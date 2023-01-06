package graboid.layer

import zio.RLayer
import farango.FarangoDatabase
import zio.ZLayer
import farango.FarangoDocumentCollection
import zio.ZIO
import ziorango.given
import zio.URIO
import graboid.layer.ArangoDBLayer

object FarangoLayer:

  val database: RLayer[ArangoDBLayer.ArangoContainer, FarangoDatabase] = ZLayer {
    for
      hostname <- ArangoDBLayer.hostname
      port     <- ArangoDBLayer.port
    yield FarangoDatabase(
      FarangoDatabase.Config(
        name = "_system",
        user = "root",
        password = "123456789",
        hosts = Seq((hostname, port))
      )
    )
  }

  def documentCollection(name: String): ZIO[FarangoDatabase, Throwable, FarangoDocumentCollection] =
    for
      database   <- ZIO.service[FarangoDatabase]
      collection <- database.documentCollection(name)
    yield collection
