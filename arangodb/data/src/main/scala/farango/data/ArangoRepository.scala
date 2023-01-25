package farango.data

import farango.FAsync
import farango.FarangoDocumentCollection

import scala.reflect.ClassTag

class ArangoRepository[T: ClassTag](collection: FarangoDocumentCollection):

  type From[I] = Conversion[I, T]

  type To[I] = Conversion[T, I]

  def database = collection.database

  def add[I: From: To, F[_]: FAsync](document: I): F[I] =
    for
      value  <- from[I, F](document)
      stored <- collection.insert[T, F](value)
      output <- to[I, F](stored)
    yield output

  def addT[O: To, F[_]: FAsync](value: T): F[O] =
    for
      inserted <- collection.insert(value)
      output   <- to[O, F](inserted)
    yield output

  def get[I: To, F[_]: FAsync](key: String): F[Option[I]] =
    for
      option: Option[T] <- collection.get(key)
      output            <- toOption(option)
    yield output

  def getT[F[_]: FAsync](key: String): F[Option[T]] =
    collection.get(key)

  def update[U: ClassTag, I: To: ClassTag, F[_]: FAsync](key: String, update: U): F[Option[I]] =
    for
      option <- collection.update[U, T, F](key, update)
      output <- toOption(option)
    yield output

  def remove[I: To, F[_]: FAsync](key: String): F[Option[I]] =
    for
      old: Option[T] <- collection.remove(key)
      output         <- toOption(old)
    yield output

  private def toOption[I: To, F[_]: FAsync](option: Option[T]): F[Option[I]] =
    option match
      case Some(value) => to[I, F](value).option
      case None        => FAsync[F].none

  private def from[A: From, F[_]: FAsync](value: A): F[T] =
    FAsync[F].succeed(summon[From[A]](value))

  private def to[A: To, F[_]: FAsync](value: T): F[A] =
    FAsync[F].succeed(summon[To[A]](value))
