package farango

import com.arangodb.ArangoDBException
import com.arangodb.async.ArangoCollectionAsync
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

  def insert[T: ClassTag, F[_]: FApplicative](document: T): F[T]

  def loadAll[T: ClassTag, S[_]: FApplicativeStream]: S[T]

  def remove[T: ClassTag, F[_]: FApplicative](key: String): F[Option[T]]

  def update[T: ClassTag, F[_]: FApplicative](key: String, document: T): F[Option[T]]

private[farango] class FarangoDocumentCollectionImpl(
    database: FarangoDatabase,
    collection: ArangoCollectionAsync
) extends FarangoDocumentCollection:

  if !collection.exists().get() then
    val creationOptions = CollectionCreateOptions()
    creationOptions.waitForSync(true)
    collection.create(creationOptions).get()

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

  override def update[T: ClassTag, F[_]: FApplicative](key: String, document: T): F[Option[T]] =
    val options = DocumentUpdateOptions()
      .returnOld(true)

    val completionStage = collection
      .updateDocument(key, document, options, expectedType)
      .exceptionallyCompose(alternativeEntity(DocumentUpdateEntity()))

    FApplicative[F].mapFromCompletionStage(completionStage)(entity => Option(entity.getOld()))

  def checkCollection[F[_]: FApplicative](): F[Boolean] =
    FApplicative[F].flatMapFromCompletionStage(collection.exists()) { exists =>
      if exists then FApplicative[F].pure(true)
      else FApplicative[F].mapFromCompletionStage(collection.create())(_ => true)
    }

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
