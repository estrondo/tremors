package tremors.zio.farango

import com.arangodb.model.DocumentDeleteOptions
import java.time.ZonedDateTime
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.zio.given
import tremors.zio.farango.DataStore.FromStore
import tremors.zio.farango.DataStore.ToStore
import zio.Task
import zio.ZIO

trait DataStore:

  def put[T: ToStore: FromStore](key: String, value: T): Task[Option[T]]

  def get[T: FromStore](key: String): Task[Option[T]]

  def remove[T: FromStore](key: String): Task[Option[T]]

object DataStore:

  def apply(suffix: String, collectionManager: CollectionManager): DataStore =
    new Impl(suffix, collectionManager)

  given ToStore[String]   = ZIO.succeed(_)
  given FromStore[String] = ZIO.succeed(_)

  given ToStore[ZonedDateTime]   = input => ZIO.attempt(input.toString)
  given FromStore[ZonedDateTime] = output => ZIO.attempt(ZonedDateTime.parse(output))

  trait ToStore[T]:
    def apply(value: T): Task[String]

  trait FromStore[T]:
    def apply(string: String): Task[T]

  case class Stored(_key: String, data: String)

  private class Impl(suffix: String, collectionManager: CollectionManager) extends DataStore:

    override def put[T: ToStore: FromStore](key: String, value: T): Task[Option[T]] =
      for
        data   <- summon[ToStore[T]](value)
        old    <- database.query[Stored, Stored](makePutQuery(key), Map("data" -> data)).runHead
        result <- old match
                    case Some(old) if old != null => summon[FromStore[T]](old.data).asSome
                    case _                        => ZIO.succeed(None)
      yield result

    private def database = collectionManager.database

    private def makePutQuery(key: String) =
      s"""
        |UPSERT {_key: '$key.$suffix'}
        |INSERT {_key: '$key.$suffix', data: @data}
        |UPDATE {data: @data} IN ${collection.name}
        |RETURN OLD""".stripMargin

    override def get[T: FromStore](key: String): Task[Option[T]] =
      for
        stored <- collection.getDocument[Stored, Stored](s"$key.$suffix")
        result <- stored match
                    case Some(stored) => summon[FromStore[T]](stored.data).asSome
                    case _            => ZIO.succeed(None)
      yield result

    private def collection = collectionManager.collection

    override def remove[T: FromStore](key: String): Task[Option[T]] =
      for
        entity <- collection.deleteDocument[Stored, Stored](s"$key.$suffix", DocumentDeleteOptions().returnOld(true))
        result <- if entity.getOld() != null then summon[FromStore[T]](entity.getOld().data).asSome
                  else ZIO.succeed(None)
      yield result
