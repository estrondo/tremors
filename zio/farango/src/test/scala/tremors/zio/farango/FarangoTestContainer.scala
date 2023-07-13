package tremors.zio.farango

import com.dimafeng.testcontainers.GenericContainer
import one.estrondo.farango.Config
import one.estrondo.farango.sync.SyncDatabase
import one.estrondo.farango.sync.SyncDB
import one.estrondo.farango.zio.given
import org.testcontainers.containers.wait.strategy.Wait
import scala.util.Random
import zio.ZIO
import zio.ZLayer

object FarangoTestContainer {

  val arangoContainer =
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

  val farangoDB = ZLayer {
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

  val farangoDatabase = ZLayer {
    for database <- ZIO.serviceWithZIO[SyncDB](_.database("test-database").create()) yield database
  }

  def farangoCollection(name: String = s"test-collection-${Random.nextInt(10)}") = ZLayer {
    for collection <- ZIO.serviceWithZIO[SyncDatabase](_.collection(name).create()) yield collection
  }

}