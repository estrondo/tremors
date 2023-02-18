package webapi.module

import com.softwaremill.macwire.wire
import webapi.config.HttpConfig
import zio.Task
import zio.ZIO
import zio.TaskLayer
import zio.ZLayer

trait HttpModule

object HttpModule:
  def apply(config: HttpConfig): Task[TaskLayer[HttpModule]] = ZIO.attempt {
    ZLayer.succeed(wire[Impl])
  }

  private class Impl() extends HttpModule
