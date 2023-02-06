package testkit.zio.testcontainers

import com.dimafeng.testcontainers.Container
import com.dimafeng.testcontainers.SingleContainer
import zio.Tag
import zio.TaskLayer
import zio.URIO
import zio.ZIO
import zio.ZLayer

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

def singleContainerGetHostname[C <: SingleContainer[?]: Tag]: URIO[C, String] =
  for service <- ZIO.service[C]
  yield service.host
