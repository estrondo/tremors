package graboid.fdsn

import com.dimafeng.testcontainers.GenericContainer
import graboid.HttpLayer
import graboid.Spec
import graboid.TimelineManager
import graboid.quakeml.QuakeMLParser
import org.testcontainers.containers.wait.strategy.Wait
import quakeml.Event
import quakeml.ResourceReference
import testkit.zio.testcontainers.*
import testkit.zio.testcontainers.given
import zio.test.TestAspect
import zio.test.assertTrue

import java.net.URL
import java.time.ZonedDateTime

object FDSNCrawlerSpec extends Spec:

  val ExposedMockserverPort = 1090

  def spec = suite("FDSN Crawler Spec")(
    suite("Using Mockserver")(
      test("should fetch events correctly.") {
        for
          port     <- singleContainerGetPort(ExposedMockserverPort)
          hostname <- singleContainerGetHostname
          window    =
            TimelineManager.Window("---", ZonedDateTime.now(), ZonedDateTime.now().plusDays(13))
          config    = FDSNCrawler.Config(
                        organization = "testable",
                        queryURL = URL(s"http://$hostname:$port/simple/fdsnws/event/1/query")
                      )
          crawler   = new FDSNCrawler(config, HttpLayer.serviceLayer, QuakeMLParser())
          stream   <- crawler.crawl(window).orDieWith(identity)
          all      <- stream.runCollect.orDieWith(identity)
        yield assertTrue(
          all.size == 3,
          all(0).asInstanceOf[Event].publicID == ResourceReference(
            "smi:org.gfz-potsdam.de/geofon/usp2022mqpz"
          ),
          all(1).asInstanceOf[Event].publicID == ResourceReference(
            "smi:org.gfz-potsdam.de/geofon/usp2022mcfz"
          ),
          all(2).asInstanceOf[Event].publicID == ResourceReference(
            "smi:org.gfz-potsdam.de/geofon/usp2022ltei"
          )
        )
      }
    ).provideLayer(dockerLayer)
  ).provideLayer(logger) @@ TestAspect.ignore

  def dockerLayer = layerOf {
    GenericContainer(
      dockerImage = "mockserver/mockserver:5.14.0",
      exposedPorts = Seq(ExposedMockserverPort),
      env = Map(
        "SERVER_PORT" -> ExposedMockserverPort.toString()
      ),
      fileSystemBind = Seq(
        "src/test/mockserver/FDSNCrawlerSpec" -> "/config"
      ),
      waitStrategy = Wait.forLogMessage(s".*started on port.*", 1)
    )
  }
