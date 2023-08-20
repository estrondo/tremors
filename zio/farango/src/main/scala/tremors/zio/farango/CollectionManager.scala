package tremors.zio.farango

import com.arangodb.ArangoDBException
import one.estrondo.farango.Collection
import one.estrondo.farango.Database
import one.estrondo.farango.zio.given
import scala.annotation.tailrec
import zio.Schedule
import zio.Task
import zio.URIO
import zio.ZIO
import zio.ZIOAspect

trait CollectionManager:

  val CollectionNotFound = 1203
  val Forbidden          = 11

  def collection: Collection

  def create(): Task[Collection]

  def sakePolicy: Schedule[Any, Throwable, Throwable]

object CollectionManager:

  def apply(
      collection: Collection,
      database: Database
  ): CollectionManager =
    new Impl(collection, database)

  private class Impl(val collection: Collection, database: Database) extends CollectionManager:

    override val sakePolicy: Schedule[Any, Throwable, Throwable] =
      Schedule.recurWhileZIO(recreate(_) @@ annotations)

    override def create(): Task[Collection] =
      (for
        _ <- shouldCreateDatabase()
        _ <- shouldCreateCollection()
      yield collection) @@ annotations

    private def annotations = ZIOAspect.annotated(
      "farango.collection" -> collection.name,
      "farango.database"   -> database.name
    )

    private def shouldCreateCollection(): Task[Boolean] =
      for
        exists <- collection.exists
        _      <- if exists then ZIO.unit
                  else collection.create().tapErrorCause(ZIO.logErrorCause("It was impossible to create collection!", _))
        _      <- ZIO.logDebug("Collection was created.")
      yield true

    private def shouldCreateDatabase(): Task[Boolean] =
      for
        exists <- database.exists
        _      <- if exists then ZIO.unit
                  else database.create().tapErrorCause(ZIO.logErrorCause("It was impossible to create database!", _))
        _      <- ZIO.logDebug("Database was created.")
      yield true

    @tailrec
    private def recreate(cause: Throwable): URIO[Any, Boolean] =
      cause match
        case null => ZIO.succeed(true)

        case exception: ArangoDBException =>
          exception.getErrorNum match
            case CollectionNotFound => shouldCreateCollection().orElseSucceed(true)
            case Forbidden          => (shouldCreateDatabase() *> shouldCreateCollection()).orElseSucceed(true)
            case _                  => ZIO.succeed(false)

        case other: Throwable => recreate(other.getCause)
