package tremors.zio.farango

import com.arangodb.model.CollectionCreateOptions
import com.dimafeng.testcontainers.GenericContainer
import one.estrondo.farango.Config
import one.estrondo.farango.sync.SyncCollection
import one.estrondo.farango.sync.SyncDatabase
import one.estrondo.farango.sync.SyncDB
import one.estrondo.farango.zio.given
import org.testcontainers.containers.wait.strategy.Wait
import scala.util.Random
import zio.ZIO
import zio.ZLayer

object FarangoTestContainer {

  val arangoContainer: ZLayer[Any, Throwable, GenericContainer] =
    val containerDef = GenericContainer
      .Def(
        dockerImage = "docker.io/rthoth/estrondo:tremors_arangodb_test_3.11.1",
        exposedPorts = Seq(8529),
        waitStrategy = Wait.forLogMessage(""".*Have fun.*""", 1)
      )

    ZLayer.scoped {
      ZIO.acquireRelease(
        ZIO.attemptBlocking {
          containerDef.start()
        }
      )(container => ZIO.attemptBlocking(container.stop()).orDie)
    }

  val farangoDB: ZLayer[GenericContainer, Throwable, SyncDB] = ZLayer {
    for
      arangoPort <- ZIO.serviceWith[GenericContainer](_.mappedPort(8529))
      db         <- ZIO.fromTry(
                      SyncDB(
                        Config()
                          .addHost("localhost", arangoPort)
                          .withUser("tremors")
                          .withPassword("tremors")
                          .withRootPassword("tremors")
                      )
                    )
      _          <- db.createDefaultUser()
    yield db
  }

  def farangoDatabase(create: Boolean = true): ZLayer[SyncDB, Throwable, SyncDatabase] = ZLayer {
    for database <- ZIO.serviceWithZIO[SyncDB](db => {
                      val database = db.database("test-database")
                      if create then database.create() else ZIO.succeed(database)
                    })
    yield database
  }

  def farangoCollection(
      name: String = s"test-collection-${Random.nextInt(10)}",
      create: Boolean = true
  ): ZLayer[SyncDatabase, Throwable, SyncCollection] = ZLayer {
    for collection <- ZIO.serviceWithZIO[SyncDatabase](database => {
                        val collection =
                          database.collection(name)
                        if create then collection.create() else ZIO.succeed(collection)
                      })
    yield collection
  }

}
