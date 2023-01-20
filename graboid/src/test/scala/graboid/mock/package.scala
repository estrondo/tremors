package graboid.mock

import graboid.EventPublisherManager
import zio.ZLayer
import one.estrondo.sweetmockito.SweetMockito

val EventPublisherManagerLayer = ZLayer.succeed(SweetMockito[EventPublisherManager])
