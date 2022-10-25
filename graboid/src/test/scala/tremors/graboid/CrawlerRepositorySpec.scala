package tremors.graboid

import zio.test.assertTrue
import zio.ZIO
import tremors.ziotestcontainers.*
import com.dimafeng.testcontainers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import farango.FarangoDatabase
import scala.concurrent.duration.Duration.apply
import java.time.Duration
import java.time.ZonedDateTime
import zio.stream.ZStream
import zio.stream.ZSink

object CrawlerRepositorySpec extends Spec:

  private val createRepository =
    for
      port    <- ArangoDBLayer.getPort()
      database = FarangoDatabase(
                   FarangoDatabase.Config(
                     name = "_system",
                     user = "root",
                     password = "159753",
                     hosts = Seq(("localhost", port))
                   )
                 )
    yield CrawlerRepository(database)

  override def spec = suite("A CrawlerRepository")(
    suite("with valid ArangoDB")(
      test("should add a new CrawlerDescriptor") {
        for
          repository <- createRepository
          descriptor  = CrawlerDescriptor(
                          name = "A test descriptor",
                          `type` = "testable",
                          source = "Sun",
                          windowDuration = Duration.ofDays(29),
                          starting = ZonedDateTime.parse("2000-01-01T00:00:00Z")
                        )
          stored     <- repository.add(descriptor)
        yield assertTrue(stored == descriptor)
      }.provideLayer(ArangoDBLayer.layer),
      test("should return stream of 10 descriptors") {
        val descriptors =
          for i <- 1 to 10
          yield CrawlerDescriptor(
            name = s"#$i",
            `type` = "testable",
            source = s"Planet #${i}",
            windowDuration = Duration.ofDays(i),
            starting = ZonedDateTime.parse("1982-06-24T04:00:00Z")
          )

        for
          repository <- createRepository
          _          <- ZStream.fromIterable(descriptors).mapZIO(repository.add).run(ZSink.drain)
          stored     <- repository.getAllDescriptors().run(ZSink.collectAll)
        yield assertTrue(stored.to(Seq) == descriptors)
      }.provideLayer(ArangoDBLayer.layer)
    )
  )
