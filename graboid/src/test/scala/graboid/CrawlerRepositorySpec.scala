package graboid

import com.dimafeng.testcontainers.GenericContainer
import farango.FarangoDatabase
import org.testcontainers.containers.wait.strategy.Wait
import graboid.command.CrawlerDescriptorFixture
import ziotestcontainers.*
import zio.ZIO
import zio.durationInt
import zio.stream.ZSink
import zio.stream.ZStream
import zio.test.TestArgs
import zio.test.TestAspect
import zio.test.assertTrue

import java.time.Duration
import java.time.ZonedDateTime
import scala.concurrent.duration.Duration.apply
import graboid.protocol.CrawlerDescriptor

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
                          key = createRandomKey(),
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
            key = createRandomKey(),
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
    ),
    test("should remove a CrawlerDescriptor") {
      val descriptor = CrawlerDescriptorFixture.createRandom()
      for
        repository <- createRepository
        _          <- repository.add(descriptor)
        old        <- repository.remove(descriptor.key)
      yield assertTrue(old == Some(descriptor))
    }.provideLayer(ArangoDBLayer.layer),
    test("should update a CrawlerDescriptor") {
      val descriptor        = CrawlerDescriptorFixture.createRandom()
      val updatedDescriptor = descriptor.copy(
        name = descriptor.name + "@",
        starting = descriptor.starting.plusDays(2)
      )

      for
        repository <- createRepository
        _          <- repository.add(descriptor)
        old        <- repository.update(updatedDescriptor)
      yield assertTrue(
        old == Some(descriptor)
      )
    }.provideLayer(ArangoDBLayer.layer)
  )
