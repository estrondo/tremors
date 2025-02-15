package tremors.zio.grpc

import io.grpc.Attributes
import io.grpc.MethodDescriptor
import scalapb.zio_grpc.RequestContext
import scalapb.zio_grpc.SafeMetadata
import zio.UIO

object RequestContextFixture:

  def createRandom[Req, Res](
      methodDescriptor: MethodDescriptor[Req, Res],
      metadata: Seq[(String, String)] = Seq.empty,
      authority: Option[String] = None,
  ): UIO[RequestContext] =
    for
      requestSm  <- SafeMetadata.make(metadata*)
      responseSm <- SafeMetadata.make
    yield RequestContext(
      metadata = requestSm,
      responseMetadata = responseSm,
      authority = authority,
      methodDescriptor = methodDescriptor,
      attributes = Attributes.newBuilder().build(),
    )
