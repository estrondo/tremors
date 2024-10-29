package graboid

import graboid.command.CommandExecutor
import graboid.command.CommandListener
import graboid.protocol.GraboidCommandFailure
import graboid.protocol.GraboidCommandFixture
import graboid.protocol.GraboidCommandSuccess
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.assertTrue

object CommandListenerSpec extends GraboidSpec:

  override def spec = suite("A CommandListener")(
    test("It should report a well-done command.") {
      val expectedCommand = GraboidCommandFixture.createDataCentre()
      for
        _        <- SweetMockitoLayer[CommandExecutor]
                      .whenF2(_.apply(expectedCommand))
                      .thenReturn(expectedCommand)
        response <- ZIO.serviceWithZIO[CommandListener](_(expectedCommand))
      yield assertTrue(
        response == GraboidCommandSuccess(expectedCommand.commandId),
      )
    },
    test("It should report a command failure.") {
      val expectedCommand = GraboidCommandFixture.createDataCentre()
      for
        _        <- SweetMockitoLayer[CommandExecutor]
                      .whenF2(_.apply(expectedCommand))
                      .thenFail(IllegalStateException("@@@"))
        response <- ZIO.serviceWithZIO[CommandListener](_(expectedCommand))
      yield assertTrue(
        response == GraboidCommandFailure(expectedCommand.commandId, "@@@"),
      )
    },
  ).provide(
    SweetMockitoLayer.newMockLayer[CommandExecutor],
    ZLayer(ZIO.serviceWithZIO[CommandExecutor](CommandListener(_))),
  )
