package farango

import com.arangodb.async.ArangoCollectionAsync
import com.arangodb.model.CollectionCreateOptions

import scala.reflect.ClassTag

trait FarangoDocumentCollection:

  def insert[T: ClassTag, F[_]: FApplicative](document: T): F[T]

private[farango] class FarangoDocumentCollectionImpl(
    database: FarangoDatabase,
    collection: ArangoCollectionAsync
) extends FarangoDocumentCollection:

  if !collection.exists().get() then
    val creationOptions = CollectionCreateOptions()
    creationOptions.waitForSync(true)
    collection.create(creationOptions).get()

  override def insert[T: ClassTag, F[_]: FApplicative](document: T): F[T] =
    val applicative = summon[FApplicative[F]]

    applicative.mapFromCompletionStage(collection.insertDocument(document)) { entity =>
      entity.getNew()
    }

  def checkCollection[F[_]: FApplicative](): F[Boolean] =
    val applicative = summon[FApplicative[F]]

    applicative.flatMapFromCompletionStage(collection.exists()) { exists =>
      if exists then applicative.pure(true)
      else applicative.mapFromCompletionStage(collection.create())(_ => true)
    }
