package graboid.mock

import graboid.PublisherManager
import one.estrondo.sweetmockito.SweetMockito
import zio.ZLayer

val PublisherManagerLayer = ZLayer.succeed(SweetMockito[PublisherManager])
