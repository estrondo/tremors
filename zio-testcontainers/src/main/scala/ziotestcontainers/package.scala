package ziotestcontainers

import com.dimafeng.testcontainers.Container

import zio.{Tag, TaskLayer, URIO, Task}
import zio.ZIO
import com.dimafeng.testcontainers.SingleContainer
import com.dimafeng.testcontainers.GenericContainer.DockerImage
import java.util.concurrent.Future
import java.util.concurrent.CompletableFuture
import zio.ZLayer
import com.dimafeng.testcontainers.GenericContainer.FileSystemBind
import org.testcontainers.containers.BindMode

given Conversion[(String, String), FileSystemBind] = (source, target) =>
  FileSystemBind(source, target, BindMode.READ_ONLY)

def layerOf[C <: Container: Tag](fn: => C): TaskLayer[C] = ZLayer.scoped {
  val acquire = ZIO.attemptBlocking {
    val container: C = fn
    container.start()
    container
  }

  val release = (container: C) => ZIO.attemptBlocking(container.stop()).ignoreLogged
  ZIO.acquireRelease(acquire)(release)
}

def singleContainerGetPort[C <: SingleContainer[?]: Tag](portNumber: Int): URIO[C, Int] =
  for service <- ZIO.service[C]
  yield service.mappedPort(portNumber)
