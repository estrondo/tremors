package graboid.timeline

import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.GenericContainer.DockerImage
import farango.FarangoDatabase
import farango.FarangoDocumentCollection
import graboid.ArangoDBLayer
import graboid.Spec
import graboid.TimelineManager
import graboid.repository.TimelineRepository
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.testcontainers.containers.wait.strategy.Wait
import testkit.zio.testcontainers.*
import testkit.zio.testcontainers.given
import zio.Task
import zio.ZIO
import zio.test.Assertion
import zio.test.TestAspect
import zio.test.assertTrue
import ziorango.Ziorango
import ziorango.given

import java.io.IOException
import java.time.Clock
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import scala.util.Failure
import scala.util.Success

object TimelineRepositorySpec extends Spec:

  private val createRepository =
    for
      port       <- ArangoDBLayer.getPort()
      hostname   <- ArangoDBLayer.getHostname()
      database    = FarangoDatabase(
                      FarangoDatabase.Config(
                        name = "_system",
                        user = "root",
                        password = "159753",
                        hosts = Seq((hostname, port))
                      )
                    )
      repository <- TimelineRepository(database)
    yield repository

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
    ).provideLayer(ArangoDBLayer.layer),
    suite("Mocking test")(
      test("should report ArangoDB's exceptions when it's searching the last window") {
        val database   = mock(classOf[FarangoDatabase])
        val collection = mock(classOf[FarangoDocumentCollection])

        val expectedThrowable = IOException("An internal error has happened!")

        when(collection.database)
          .thenReturn(database)
          
        when(database.query(anyString(), any())(any(), any(), any()))
          .thenReturn(ZIO.fail(expectedThrowable))

        val repository = TimelineRepository(collection)
        for result <- repository.last("crawler-01").fold(identity, _ => "Ops!")
        yield assertTrue(result == expectedThrowable)
      },
      test("should report ArangoBD's exceptions when it's inserting a new window") {
        val database   = mock(classOf[FarangoDatabase])
        val collection = mock(classOf[FarangoDocumentCollection])

        val expectedThrowable = IOException("OMG!")

        when(collection.insert(any())(any(), any()))
          .thenReturn(ZIO.fail(expectedThrowable))

        val repository = TimelineRepository(collection)
        for result <-
            repository
              .add("omg", TimelineManager.Window("---", ZonedDateTime.now(), ZonedDateTime.now()))
              .fold(identity, _ => "Ops!")
        yield assertTrue(result == expectedThrowable)
      }
    )
  ).provideLayer(logger)
