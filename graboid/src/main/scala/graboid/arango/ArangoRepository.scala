package graboid.arango

import farango.FarangoDocumentCollection
import io.netty.util.Mapping
import zio.Task
import zio.ZIO
import ziorango.Ziorango
import ziorango.given

import scala.Conversion
import scala.reflect.ClassTag

class ArangoRepository[T: ClassTag](collection: FarangoDocumentCollection):

  type From[I] = Conversion[I, T]

  type To[I] = Conversion[T, I]

  def database = collection.database

  def add[I: From: To](document: I): Task[I] =
    for
      value  <- from[I](document)
      stored <- collection.insert(value)
      output <- to[I](stored)
    yield output

  def addT[O: To](value: T): Task[O] =
    for
      inserted <- collection.insert(value)
      output   <- to[O](inserted)
    yield output

  def get[I: To](key: String): Task[Option[I]] =
    for
      option: Option[T] <- collection.get(key)
      output            <- toOption(option)
    yield output

  def getT(key: String): Task[Option[T]] =
    collection.get(key)

  def update[U: ClassTag, I: To: ClassTag](key: String, update: U): Task[Option[I]] =
    for
      option <- collection.update[U, T, Ziorango.F](key, update)
      output <- toOption(option)
    yield output

  def remove[I: To](key: String): Task[Option[I]] =
    for
      old: Option[T] <- collection.remove(key)
      output         <- toOption(old)
    yield output

  private def toOption[I: To](option: Option[T]): Task[Option[I]] =
    option match
      case Some(value) => to[I](value).option
      case None        => ZIO.none

  private def from[A: From](value: A): Task[T] =
    ZIO.attempt(summon[From[A]](value))

  private def to[A: To](value: T): Task[A] =
    ZIO.attempt(summon[To[A]](value))
