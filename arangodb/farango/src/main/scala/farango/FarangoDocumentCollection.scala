package farango

import com.arangodb.ArangoDBException
import com.arangodb.async.ArangoCollectionAsync
import com.arangodb.async.ArangoDatabaseAsync
import com.arangodb.entity.CollectionEntity
import com.arangodb.entity.DocumentDeleteEntity
import com.arangodb.entity.DocumentUpdateEntity
import com.arangodb.model.CollectionCreateOptions
import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentDeleteOptions
import com.arangodb.model.DocumentUpdateOptions

import java.util.Collections
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import scala.reflect.ClassTag

trait FarangoDocumentCollection:

  def database: FarangoDatabase

  def name: String

  def get[T: ClassTag, F[_]: FApplicative](key: String): F[Option[T]]

  def insert[T: ClassTag, F[_]: FApplicative](document: T): F[T]

  def loadAll[T: ClassTag, S[_]: FApplicativeStream]: S[T]

  def remove[T: ClassTag, F[_]: FApplicative](key: String): F[Option[T]]

  def update[U: ClassTag, T: ClassTag, F[_]: FApplicative](key: String, document: U): F[Option[T]]

object FarangoDocumentCollection:

  def apply[F[_]: FApplicative](
      name: String,
      database: FarangoDatabase
  ): F[FarangoDocumentCollection] =

    val collection = database.underlying.collection(name)
    val response   = collection.exists().thenComposeAsync { exists =>
      if exists then CompletableFuture.completedFuture(())
      else create(collection)
    }

    FApplicative[F].mapFromCompletionStage(response)(_ => FarangoDocumentCollectionImpl(database, collection))

  private def create(collection: ArangoCollectionAsync): CompletableFuture[Unit] =
    val options = CollectionCreateOptions()
      .waitForSync(true)
    collection.create(options).thenApply(_ => ())

private[farango] class FarangoDocumentCollectionImpl(
    val database: FarangoDatabase,
    collection: ArangoCollectionAsync
) extends FarangoDocumentCollection:

  override def name: String = collection.name()

  override def get[T: ClassTag, F[_]: FApplicative](key: String): F[Option[T]] =
    FApplicative[F].mapFromCompletionStage(collection.getDocument(key, expectedType[T]))(Option(_))

  override def insert[T: ClassTag, F[_]: FApplicative](document: T): F[T] =
    val options = DocumentCreateOptions()
      .returnNew(true)

    FApplicative[F].mapFromCompletionStage(collection.insertDocument(document, options)) { entity =>
      entity.getNew()
    }

  override def loadAll[T: ClassTag, S[_]: FApplicativeStream]: S[T] =
    val completionStage = database.underlying
      .query(
        s"FOR e IN ${collection.name()} RETURN e",
        summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
      )
      .thenApply(cursor => cursor.streamRemaining())

    FApplicativeStream[S].mapFromCompletionStage(completionStage)(identity)

  override def remove[T: ClassTag, F[_]: FApplicative](key: String): F[Option[T]] =
    val options = DocumentDeleteOptions()
      .returnOld(true)

    val completionStage = collection
      .deleteDocument(key, expectedType, options)
      .exceptionallyCompose(alternativeEntity(DocumentDeleteEntity()))

    FApplicative[F].mapFromCompletionStage(completionStage)(entity => Option(entity.getOld()))

  override def update[U: ClassTag, T: ClassTag, F[_]: FApplicative](
      key: String,
      document: U
  ): F[Option[T]] =
    val options = DocumentUpdateOptions()
      .returnOld(true)

    val completionStage = collection
      .updateDocument(key, document, options, expectedType[T])
      .exceptionallyCompose(alternativeEntity(DocumentUpdateEntity()))

    FApplicative[F].mapFromCompletionStage(completionStage)(entity => Option(entity.getOld()))

  private inline def expectedType[T](using tag: ClassTag[T]): Class[T] =
    tag.runtimeClass.asInstanceOf[Class[T]]

  private def alternativeEntity[T](entity: T)(
      throwable: Throwable
  ): CompletionStage[T] =
    throwable match
      case wrapper: CompletionException =>
        alternativeEntity(entity)(wrapper.getCause())

      case cause: ArangoDBException if cause.getErrorNum() == 1202 =>
        CompletableFuture.completedFuture(entity)

      case _ => CompletableFuture.failedStage(throwable)
