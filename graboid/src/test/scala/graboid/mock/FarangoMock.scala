package graboid.mock

import farango.DocumentCollection
import org.mockito.Mockito
import zio.ZLayer

val FarangoDocumentCollectionMockLayer =
  ZLayer.succeed(Mockito.mock(classOf[DocumentCollection]))
