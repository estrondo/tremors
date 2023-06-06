package testkit.zio.testcontainers

import com.arangodb.async.ArangoDBAsync
import com.arangodb.mapping.ArangoJack
import com.bedatadriven.jackson.datatype.jts.JtsModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import farango.Database
import farango.DocumentCollection
import farango.data.FarangoModule
import farango.zio.given
import scala.reflect.ClassTag
import testkit.core.createRandomKey
import zio.RIO
import zio.RLayer
import zio.ZIO
import zio.ZLayer

object FarangoLayer:

  val database: RLayer[ArangoDBLayer.ArangoContainer, Database] = ZLayer {
    for
      hostname <- ArangoDBLayer.hostname
      port     <- ArangoDBLayer.port
      database <- {
        val serializer = ArangoJack()
        serializer.configure(mapper =>
          mapper
            .registerModule(DefaultScalaModule)
            .registerModule(FarangoModule)
            .registerModule(new JtsModule())
        )

        val dbAsync = ArangoDBAsync
          .Builder()
          .user("root")
          .password("123456789")
          .host(hostname, port)
          .serializer(serializer)
          .build()

        Database(dbAsync.db(s"it_test_database${createRandomKey()}"))
      }
    yield database

  }

  def documentCollectionLayer(name: String): RLayer[Database, DocumentCollection] = ZLayer {
    ZIO.serviceWithZIO[Database](_.documentCollection(name))
  }

  def documentCollection(name: String): ZIO[Database, Throwable, DocumentCollection] =
    for
      database   <- ZIO.service[Database]
      collection <- database.documentCollection(name).orDie
    yield collection

  def getDocument[T: ClassTag, A](key: String)(using Conversion[T, A]): RIO[DocumentCollection, Option[A]] =
    for
      collection <- ZIO.service[DocumentCollection]
      result     <- collection.get[T](key)
    yield result
