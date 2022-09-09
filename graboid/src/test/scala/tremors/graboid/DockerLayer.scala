package tremors.graboid

import zio.RLayer
import com.dimafeng.testcontainers.{GenericContainer, SingleContainer}
import zio.ZLayer
import zio.{ZIO, Task, TaskLayer, UIO, URIO}
import scala.util.Try
import com.dimafeng.testcontainers.SingleContainer
import com.dimafeng.testcontainers.GenericContainer.DockerImage
import cats.instances.set
import org.testcontainers.containers.wait.strategy.WaitStrategy
import org.testcontainers.containers.wait.strategy.Wait
import com.dimafeng.testcontainers.GenericContainer.FileSystemBind
import org.testcontainers.containers.BindMode
import java.io.File

object DockerLayer:

  given Conversion[(String, String), FileSystemBind] = (hostPath, containerPath) =>
    FileSystemBind(hostPath, containerPath, BindMode.READ_ONLY)

  case class Def(
      image: String,
      exposedPorts: Seq[Int] = Nil,
      env: Map[String, String] =   Map.empty,
      waitStrategy: WaitStrategy = Wait.forListeningPort(),
      volumes: Seq[FileSystemBind] = Nil
  )

  class SingleContainer(underling: GenericContainer):
    underling.start()

    def stop(): Unit = underling.stop()

    def port(portNumber: Int) = underling.mappedPort(portNumber)

  def singleContainerLayer(settings: Def): TaskLayer[SingleContainer] =
    ZLayer.scoped {
      val acquire = ZIO.attempt(
        SingleContainer(
          GenericContainer(
            dockerImage = DockerImage(Left(settings.image)),
            env = settings.env,
            exposedPorts = settings.exposedPorts,
            waitStrategy = settings.waitStrategy,
            fileSystemBind = settings.volumes
          )
        )
      )

      val release = (container: SingleContainer) => ZIO.attempt(container.stop()).ignoreLogged
      ZIO.acquireRelease(acquire)(release)
    }

  def singleContainerPort(portNumber: Int): URIO[SingleContainer, Int] =
    ZIO.serviceWith[SingleContainer](_.port(portNumber))
