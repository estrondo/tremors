package testkit.zio.testcontainers

import com.arangodb.async.ArangoDBAsync
import com.arangodb.mapping.ArangoJack
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import farango.Database
import farango.DocumentCollection
import farango.zio.given
import zio.RIO
import zio.RLayer
import zio.ZIO
import zio.ZLayer

import scala.reflect.ClassTag

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
