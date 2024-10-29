package tremors.zio.farango

import com.arangodb.ArangoDBException
import one.estrondo.farango.Collection
import one.estrondo.farango.Database
import one.estrondo.farango.zio.given
import scala.annotation.tailrec
import zio.Schedule
import zio.Schedule.WithState
import zio.Task
import zio.URIO
import zio.ZIO
import zio.ZIOAspect
import zio.Zippable

trait CollectionManager:

  val CollectionNotFound = 1203
  val Forbidden          = 11

  def collection: Collection

  def database: Database

  def create(): Task[Collection]

  def sakePolicy: Schedule.WithState[(Long, Unit), Any, Throwable, Zippable[Long, Throwable]#Out]

object CollectionManager:

  def apply(
      collection: Collection,
      database: Database,
      policy: Schedule.WithState[Long, Any, Any, Long],
  ): CollectionManager =
    new Impl(collection, database, policy)

  private class Impl(
      val collection: Collection,
      val database: Database,
      policy: Schedule.WithState[Long, Any, Any, Long],
  ) extends CollectionManager:

    override val sakePolicy: WithState[(Long, Unit), Any, Throwable, Zippable[Long, Throwable]#Out] =
      policy && Schedule.recurWhileZIO[Any, Throwable](recreate(_) @@ annotations)

    override def create(): Task[Collection] =
      (for
        _ <- shouldCreateDatabase()
        _ <- shouldCreateCollection()
      yield collection) @@ annotations

    private def annotations = ZIOAspect.annotated(
      "farango.collection" -> collection.name,
      "farango.database"   -> database.name,
    )

    private def shouldCreateCollection(): Task[Unit] =
      val checkCreated = for
        exists <- database.exists
        _      <- if exists then ZIO.logDebug("Collection was created.") else ZIO.unit
      yield exists

      for
        exists <- collection.exists
        _      <- if exists then ZIO.unit
                  else collection.create().tapErrorCause(ZIO.logErrorCause("It was impossible to create collection!", _))
        _      <- checkCreated.repeat(policy && Schedule.recurUntil[Boolean](identity))
      yield ()

    private def shouldCreateDatabase(): Task[Unit] =
      val checkCreated = for
        exists <- database.exists
        _      <- if exists then ZIO.logDebug("Database was created.") else ZIO.unit
      yield exists

      for
        exists <- database.exists
        _      <- if exists then ZIO.unit
                  else database.create().tapErrorCause(ZIO.logErrorCause("It was impossible to create database!", _))
        _      <- checkCreated.repeat(policy && Schedule.recurUntil[Boolean](identity))
      yield ()

    @tailrec
    private def recreate(cause: Throwable): URIO[Any, Boolean] =
      cause match
        case null =>
          ZIO.succeed(false)

        case exception: ArangoDBException =>
          exception.getErrorNum match
            case CollectionNotFound                                             =>
              shouldCreateCollection().fold(_ => true, _ => true)
            case Forbidden                                                      =>
              (shouldCreateDatabase() *> shouldCreateCollection()).fold(_ => true, _ => true)
            case _ if exception.getMessage.contains("Cannot contact any host!") =>
              ZIO.logDebug("It is waiting for ArangoDB.").as(true)
            case _ if exception.getResponseCode == 503                          =>
              ZIO.logDebug("ArangoDB is not available, waiting for it.").as(true)
            case _                                                              =>
              ZIO.succeed(false)

        case other: Throwable =>
          recreate(other.getCause)
