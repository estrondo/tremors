package tremors.graboid.timeline

import farango.FarangoDatabase
import org.testcontainers.containers.wait.strategy.Wait
import tremors.graboid.DockerLayer
import tremors.graboid.Spec
import tremors.graboid.repository.TimelineRepository
import zio.test.assertTrue

import java.time.Clock
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object TimelineRepositorySpec extends Spec:

  private val createRepository =
    for
      port    <- DockerLayer.getPort(8529)
      database = FarangoDatabase(
                   FarangoDatabase.Config(
                     name = "_system",
                     user = "root",
                     password = "159753",
                     hosts = Seq(("localhost", port))
                   )
                 )
    yield TimelineRepository(database)

  private val arangoDBLayer = DockerLayer.singleContainerLayer(
    DockerLayer.Def(
      image = "arangodb/arangodb:3.10.0",
      env = Map(
        "ARANGO_ROOT_PASSWORD" -> "159753"
      ),
      exposedPorts = Seq(8529),
      waitStrategy = Wait.forLogMessage(".*Have fun!.*", 1)
    )
  )

  override def spec = suite("A TimelineRepository")(
    test("should store a new window") {
      val beginning = ZonedDateTime.now()
      val ending    = beginning.plusDays(13)

      for
        repository <- createRepository
        stored     <- repository.add("zyb", beginning, ending)
      yield assertTrue(stored == (Some(beginning), Some(ending)))
    }.provideLayer(arangoDBLayer),
    test("should retrieve the last window") {
      val beginning = ZonedDateTime
        .now(Clock.systemUTC())
        .plusDays(13)
        .truncatedTo(ChronoUnit.SECONDS)

      val ending = beginning.plusDays(29)

      for
        repository <- createRepository
        _          <- repository.add("hello", beginning, ending)
        retrieved  <- repository.last("hello")
      yield assertTrue(retrieved == Some((Some(beginning), Some(ending))))
    }.provideLayer(arangoDBLayer)
  )
