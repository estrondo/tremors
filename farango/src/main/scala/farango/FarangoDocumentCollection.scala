package farango

import com.arangodb.async.ArangoCollectionAsync
import com.arangodb.model.CollectionCreateOptions

import scala.reflect.ClassTag

trait FarangoDocumentCollection:

  def insert[T: ClassTag, F[_]: FarangoEffect](document: T): F[T]

private[farango] class FarangoDocumentCollectionImpl(
    database: FarangoDatabase,
    collection: ArangoCollectionAsync
) extends FarangoDocumentCollection:

  if !collection.exists().get() then
    val creationOptions = CollectionCreateOptions()
    creationOptions.waitForSync(true)
    collection.create(creationOptions).get()

  override def insert[T: ClassTag, F[_]: FarangoEffect](document: T): F[T] =
    val effect = summon[FarangoEffect[F]]

    effect.mapFromCompletionStage(collection.insertDocument(document)) { entity =>
      entity.getNew()
    }

  def checkCollection[F[_]: FarangoEffect](): F[Boolean] =
    val effect = summon[FarangoEffect[F]]

    effect.flatMapFromCompletionStage(collection.exists()) { exists =>
      if exists then effect.pure(true)
      else effect.mapFromCompletionStage(collection.create())(_ => true)
    }
