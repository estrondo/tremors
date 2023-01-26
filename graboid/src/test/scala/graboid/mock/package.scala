package graboid.mock

import graboid.PublisherManager
import zio.ZLayer
import one.estrondo.sweetmockito.SweetMockito

val PublisherManagerLayer = ZLayer.succeed(SweetMockito[PublisherManager])
