package graboid.mock

import farango.FarangoDocumentCollection
import org.mockito.Mockito
import zio.ZLayer

val FarangoDocumentCollectionMockLayer =
  ZLayer.succeed(Mockito.mock(classOf[FarangoDocumentCollection]))
