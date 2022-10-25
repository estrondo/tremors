package tremors.graboid

import com.dimafeng.testcontainers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import tremors.ziotestcontainers.*
import zio.TaskLayer
import zio.URIO
import zio.ZIO

object ArangoDBLayer:

  opaque type ArangoContainer = GenericContainer

  val layer: TaskLayer[ArangoContainer] = layerOf {
    GenericContainer(
      dockerImage = "arangodb/arangodb:3.10.0",
      env = Map(
        "ARANGO_ROOT_PASSWORD" -> "159753"
      ),
      exposedPorts = Seq(8529),
      waitStrategy = Wait.forLogMessage(".*Have fun!.*", 1)
    ).asInstanceOf[ArangoContainer]
  }

  def getPort(): URIO[ArangoContainer, Int] =
    ZIO.serviceWithZIO { container =>
      ZIO.succeed(container.mappedPort(8529))
    }
