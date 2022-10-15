package tremors.graboid.fdsn

import com.dimafeng.testcontainers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import tremors.graboid.Spec
import tremors.graboid.TimelineManager
import tremors.graboid.WithHttpLayer
import tremors.graboid.WithHttpServiceLayer
import tremors.graboid.quakeml.QuakeMLParser
import tremors.quakeml.Event
import tremors.quakeml.ResourceReference
import tremors.ziotestcontainers.*
import tremors.ziotestcontainers.given
import zio.test.assertTrue

import java.net.URL
import java.time.ZonedDateTime

object FDSNCrawlerSpec extends Spec with WithHttpServiceLayer with WithHttpLayer:

  val ExposedMockserverPort = 1090

  def spec = suite("FDSN Crawler Spec")(
    suite("Using Mockserver")(
      test("should fetch events correctly.") {
        for
          port   <- singleContainerGetPort(ExposedMockserverPort)
          window  =
            TimelineManager.Window("---", ZonedDateTime.now(), ZonedDateTime.now().plusDays(13))
          config  = FDSNCrawler.Config(
                      organization = "testable",
                      queryURL = URL(s"http://localhost:$port/simple/fdsnws/event/1/query")
                    )
          crawler = FDSNCrawler(config, httpServiceLayer, QuakeMLParser())
          stream <- crawler.crawl(window).orDieWith(identity)
          all    <- stream.runCollect.orDieWith(identity)
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
  ).provideLayer(logger)

  def dockerLayer = layerOf {
    GenericContainer(
      dockerImage = "mockserver/mockserver:latest",
      exposedPorts = Seq(ExposedMockserverPort),
      env = Map(
        "SERVER_PORT" -> ExposedMockserverPort.toString()
      ),
      fileSystemBind = Seq(
        "src/test/mockserver/FDSNCrawlerSpec" -> "/config"
      ),
      waitStrategy = Wait.forLogMessage(s".*port: $ExposedMockserverPort.*", 1)
    )
  }
