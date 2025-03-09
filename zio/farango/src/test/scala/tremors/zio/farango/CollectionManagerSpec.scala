package tremors.zio.farango

import one.estrondo.farango.Collection
import one.estrondo.farango.ducktape.given
import one.estrondo.farango.sync.SyncDatabase
import one.estrondo.farango.sync.SyncDB
import one.estrondo.farango.zio.given
import zio.Runtime
import zio.Schedule
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.durationInt
import zio.logging.backend.SLF4J
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestClock
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.assertTrue
import zio.test.testEnvironment

object CollectionManagerSpec extends ZIOSpecDefault:

  override val bootstrap: ZLayer[Any, Any, TestEnvironment] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j >>> testEnvironment

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("A CollectionManagerSpec")(
      test("It should recreate the whole context of a collection.") {
        val domain = Domain("123", "Ronaldo")

        for
          manager  <- ZIO.service[CollectionManager]
          fiberRef <- (ZIO.logInfo("Trying to insert a new document!") *> manager.collection
                        .insertDocument[Stored, Stored](domain)
                        .retry(manager.sakePolicy)
                        .tapErrorCause(ZIO.logErrorCause("It was impossible to add a document!", _))).fork
          _        <- TestClock.adjust(1.minute)
          _        <- fiberRef.join
        yield assertTrue(true)
      }.provideSome[SyncDB](collectionManagerLayer(createDatabase = false, createCollection = false)),
      test("It should recreate only the collection.") {
        val domain = Domain("123", "Ronaldo")

        for
          manager  <- ZIO.service[CollectionManager]
          fiberRef <- (ZIO.logInfo("Trying to insert a new document!") *> manager.collection
                        .insertDocument[Stored, Stored](domain)
                        .retry(manager.sakePolicy)
                        .tapErrorCause(ZIO.logErrorCause("It was impossible to add a document!", _))).fork
          _        <- TestClock.adjust(1.minute)
          _        <- fiberRef.join
        yield assertTrue(true)
      }.provideSome[SyncDB](collectionManagerLayer(createDatabase = true, createCollection = false)),
    ).provideSome[Scope](
      FarangoTestContainer.arangoContainer,
      FarangoTestContainer.farangoDB,
    ) @@ TestAspect.sequential

  private def collectionManagerLayer(createDatabase: Boolean, createCollection: Boolean) =
    FarangoTestContainer.farangoDatabase(create = createDatabase) >+>
      FarangoTestContainer.farangoCollection(create = createCollection) >+>
      ZLayer {
        for
          database   <- ZIO.service[SyncDatabase]
          collection <- ZIO.service[Collection]
        yield CollectionManager(collection, database, Schedule.spaced(5.seconds))
      }

  case class Stored(_key: String, name: String)

  case class Domain(_key: String, name: String)
