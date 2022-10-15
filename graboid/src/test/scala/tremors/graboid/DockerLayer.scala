package tremors.graboid

import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.GenericContainer.DockerImage
import com.dimafeng.testcontainers.GenericContainer.FileSystemBind
import com.dimafeng.testcontainers.LazyContainer
import com.dimafeng.testcontainers.MultipleContainers
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.wait.strategy.WaitStrategy
import zio.TaskLayer
import zio.URIO
import zio.ZIO
import zio.ZLayer
import org.testcontainers.containers.{GenericContainer => JavaGenericContainer}

import scala.collection.immutable.HashMap
import java.net.URI
import org.testcontainers.containers.Network
import zio.Tag

object DockerLayer:

  given Conversion[(String, String), FileSystemBind] = (hostPath, containerPath) =>
    FileSystemBind(hostPath, containerPath, BindMode.READ_ONLY)

  case class Def(
      image: String,
      exposedPorts: Seq[Int] = Nil,
      env: Map[String, String] = Map.empty,
      waitStrategy: WaitStrategy = Wait.defaultWaitStrategy(),
      volumes: Seq[FileSystemBind] = Nil,
      name: Option[String] = None,
      configure: JavaGenericContainer[?] => Unit = identity
  )

  class SingleContainerService(underling: GenericContainer):
    underling.start()

    def stop(): Unit = underling.stop()

    def port(portNumber: Int) = underling.mappedPort(portNumber)

  class MultipleContainerService(
      containerMap: Map[String, GenericContainer],
      containers: Seq[LazyContainer[?]]
  ):

    private val underlying = MultipleContainers(containers: _*)
    underlying.start()

    def stop(): Unit = underlying.stop()

    def port(containerName: String, portNumber: Int): Int =
      containerMap(containerName).mappedPort(portNumber)

  def singleContainerLayer(definition: Def): TaskLayer[SingleContainerService] =
    ZLayer.scoped {
      val acquire = ZIO.attempt(
        SingleContainerService(createGenericContainer(definition, None))
      )

      val release =
        (container: SingleContainerService) => ZIO.attempt(container.stop()).ignoreLogged
      ZIO.acquireRelease(acquire)(release)
    }

  def userContainerLayer[
      JC <: org.testcontainers.containers.GenericContainer[?],
      C <: com.dimafeng.testcontainers.SingleContainer[JC]: Tag
  ](fn: => C): TaskLayer[C] =
    ZLayer.scoped {
      val acquire = ZIO.attempt {
        val container = fn
        container.start()
        container
      }

      val release = (container: C) => ZIO.attempt(container.stop()).ignoreLogged
      ZIO.acquireRelease(acquire)(release)
    }

  def multipleContainerLayer(definitions: Def*): TaskLayer[MultipleContainerService] =
    ZLayer.scoped {
      val acquire = ZIO.attempt {

        var containerMap = HashMap.empty[String, GenericContainer]
        var containers   = Seq.empty[LazyContainer[?]]
        val network      = Some(Network.newNetwork())

        for definition <- definitions do
          val container = createGenericContainer(definition, network)
          containers :+= container
          definition.name match
            case Some(containerName) => containerMap += (containerName -> container)
            case _                   =>

        MultipleContainerService(containerMap, containers)
      }

      val release =
        (container: MultipleContainerService) => ZIO.attempt(container.stop()).ignoreLogged
      ZIO.acquireRelease(acquire)(release)
    }

  def getPort(portNumber: Int): URIO[SingleContainerService, Int] =
    ZIO.serviceWith[SingleContainerService](_.port(portNumber))

  def getPort(containerName: String, portNumber: Int): URIO[MultipleContainerService, Int] =
    ZIO.serviceWith[MultipleContainerService](_.port(containerName, portNumber))

  private def createGenericContainer(definition: Def, network: Option[Network]): GenericContainer =
    val container = GenericContainer(
      dockerImage = DockerImage(Left(definition.image)),
      env = definition.env,
      exposedPorts = definition.exposedPorts,
      waitStrategy = definition.waitStrategy,
      fileSystemBind = definition.volumes
    )

    if (network.isDefined)
      container.configure(_.withNetwork(network.get))
    container.configure(definition.configure)
