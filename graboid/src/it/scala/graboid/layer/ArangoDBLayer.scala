package graboid.layer

import com.dimafeng.testcontainers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import testkit.zio.testcontainers.*
import zio.TaskLayer
import zio.URIO
import zio.ZIO

object ArangoDBLayer:

  opaque type ArangoContainer = GenericContainer

  val layer: TaskLayer[ArangoContainer] = layerOf {
    GenericContainer(
      dockerImage = "docker.io/rthoth/estrondo:arangodb_test_3.10.0",
      exposedPorts = Seq(8529),
      waitStrategy = Wait.forLogMessage(".*Have fun!.*", 1)
    )
  }

  def port: URIO[ArangoContainer, Int] =
    ZIO.serviceWith { container =>
      container.mappedPort(8529)
    }

  def hostname: URIO[ArangoContainer, String] =
    ZIO.serviceWith[ArangoContainer] { container =>
      container.host
    }
