package farango

import com.arangodb.async.ArangoCollectionAsync
import com.arangodb.model.CollectionCreateOptions

import scala.reflect.ClassTag
import java.util.Collections
import com.arangodb.model.DocumentCreateOptions

trait FarangoDocumentCollection:

  def insert[T: ClassTag, F[_]: FApplicative](document: T): F[T]

  def loadAll[T: ClassTag, S[_]: FApplicativeStream]: S[T]

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

  def checkCollection[F[_]: FApplicative](): F[Boolean] =
    FApplicative[F].flatMapFromCompletionStage(collection.exists()) { exists =>
      if exists then FApplicative[F].pure(true)
      else FApplicative[F].mapFromCompletionStage(collection.create())(_ => true)
    }
