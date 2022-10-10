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
import zio.test.Assertion
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.*
import ziorango.given
import ziorango.Ziorango
import zio.ZIO
import zio.Task
import java.io.IOException
import farango.FarangoDocumentCollection
import zio.test.TestAspect
import scala.util.Failure
import scala.util.Success
import tremors.graboid.TimelineManager

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
    suite("Integration test with ArangoDB")(
      test("should store a new window") {
        val beginning      = ZonedDateTime.now()
        val ending         = beginning.plusDays(13)
        val expectedWindow = TimelineManager.Window("---", beginning, ending)

        for
          repository <- createRepository
          stored     <- repository.add("crawler-01", expectedWindow)
        yield assertTrue(stored == expectedWindow)
      },
      test("should retrieve the last window") {
        val beginning = ZonedDateTime
          .now(Clock.systemUTC())
          .plusDays(13)
          .truncatedTo(ChronoUnit.SECONDS)

        val ending         = beginning.plusDays(29)
        val expectedWindow = TimelineManager.Window("---", beginning, ending)

        for
          repository <- createRepository
          _          <- repository.add("crawler-02", expectedWindow)
          retrieved  <- repository.last("crawler-02")
        yield assertTrue(retrieved == Some(expectedWindow))
      },
      test("should return no window when the timeline is empty") {
        for
          repository <- createRepository
          retrieve   <- repository.last("empty")
        yield assertTrue(retrieve.isEmpty)
      }
    ).provideLayer(arangoDBLayer),
    suite("Mocking test")(
      test("should report ArangoDB's exceptions when it's searching the last window") {
        val database   = mock(classOf[FarangoDatabase])
        val collection = mock(classOf[FarangoDocumentCollection])

        when(database.documentCollection(anyString()))
          .thenReturn(collection)

        val expectedThrowable = IOException("An internal error has happened!")

        when(database.query(anyString(), any())(any(), any(), any()))
          .thenReturn(ZIO.fail(expectedThrowable))

        val repository = TimelineRepository(database)
        for result <- repository.last("crawler-01").fold(identity, _ => "Ops!")
        yield assertTrue(result == expectedThrowable)
      },
      test("should report ArangoBD's exceptions when it's inserting a new window") {
        val database   = mock(classOf[FarangoDatabase])
        val collection = mock(classOf[FarangoDocumentCollection])

        when(database.documentCollection(anyString()))
          .thenReturn(collection)

        val expectedThrowable = IOException("OMG!")

        when(collection.insert(any())(any(), any()))
          .thenReturn(ZIO.fail(expectedThrowable))

        val repository = TimelineRepository(database)
        for result <-
            repository
              .add("omg", TimelineManager.Window("---", ZonedDateTime.now(), ZonedDateTime.now()))
              .fold(identity, _ => "Ops!")
        yield assertTrue(result == expectedThrowable)
      }
    )
  ).provideLayer(logger)
