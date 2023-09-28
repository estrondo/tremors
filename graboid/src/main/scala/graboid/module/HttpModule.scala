package graboid.module

import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.http.Client

trait HttpModule:

  def client: TaskLayer[Client]

object HttpModule:

  def apply(): Task[HttpModule] =
    ZIO.succeed(new Impl)

  private class Impl() extends HttpModule:

    override val client: TaskLayer[Client] = Client.default
