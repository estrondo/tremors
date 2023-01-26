package graboid.layer

import zio.RLayer
import farango.Database
import zio.ZLayer
import farango.DocumentCollection
import zio.ZIO
import zio.URIO
import graboid.layer.ArangoDBLayer
import com.arangodb.async.ArangoDBAsync
import farango.zio.given
import com.arangodb.mapping.ArangoJack
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object FarangoLayer:

  val database: RLayer[ArangoDBLayer.ArangoContainer, Database] = ZLayer {
    for
      hostname <- ArangoDBLayer.hostname
      port     <- ArangoDBLayer.port
    yield

      val serializer = ArangoJack()
      serializer.configure(mapper => mapper.registerModule(DefaultScalaModule))

      val dbAsync = ArangoDBAsync
        .Builder()
        .user("root")
        .password("123456789")
        .host(hostname, port)
        .serializer(serializer)
        .build()

      Database(dbAsync.db("_system"))
  }

  def documentCollection(name: String): ZIO[Database, Throwable, DocumentCollection] =
    for
      database   <- ZIO.service[Database]
      collection <- database.documentCollection(name).orDie
    yield collection
