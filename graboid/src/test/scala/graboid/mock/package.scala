package graboid.mock

import graboid.EventPublisherManager
import testkit.core.SweetMockito
import zio.ZLayer

val EventPublisherManagerLayer = ZLayer.succeed(SweetMockito[EventPublisherManager])
