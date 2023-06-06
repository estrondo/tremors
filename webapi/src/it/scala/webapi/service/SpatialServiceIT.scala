package webapi.service

import core.KeyGenerator
import toph.grpc.{Event => TophGRPCEvent}
import toph.grpc.{EventQuery => TophGRPCEventQuery}
import toph.grpc.ZioGrpc.{SpatialServiceClient => TophSpatialServiceClient}
import webapi.grpc.{Event => GRPCEvent}
import webapi.grpc.{EventQuery => GRPCEventQuery}
import webapi.grpc.ZioGrpc.SpatialServiceClient
import webapi.grpc.ZioGrpc.ZSpatialService
import io.bullet.borer.derivation.key
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import scalapb.zio_grpc.RequestContext
import testkit.core.createZonedDateTime
import testkit.zio.grpc.GRPC
import webapi.IT
import webapi.converter.TophEventQueryConverter
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.TestEnvironment
import zio.test.assertTrue

object SpatialServiceIT extends IT:

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("A SpatialService")(
      test("It should search for events.") {

        val time = createZonedDateTime().toString()

        val expectedQuery = GRPCEventQuery(
          boundary = Seq(1, 2, 3, 4, 5, 6),
          boundaryRadius = Some(75000),
          startTime = Some(time),
          endTime = Some(time),
          minDepth = Some(900),
          maxDepth = Some(15000),
          minMagnitude = Some(5.5),
          maxMagnitude = Some(6.7),
          magnitudeType = Seq("EBC")
        )

        val expectedTophEventQuery = TophGRPCEventQuery(
          boundary = Seq(1, 2, 3, 4, 5, 6),
          boundaryRadius = Some(75000),
          startTime = Some(time),
          endTime = Some(time),
          minDepth = Some(900),
          maxDepth = Some(15000),
          minMagnitude = Some(5.5),
          maxMagnitude = Some(6.7),
          magnitudeType = Seq("EBC")
        )

        val tophEvent = TophGRPCEvent(
          key = KeyGenerator.next8(),
          eventKey = KeyGenerator.next8(),
          hypocentreKey = Some(KeyGenerator.next8()),
          magnitudeKey = Some(KeyGenerator.next8())
        )
        val expectedEvent = GRPCEvent(
          key = Some(tophEvent.key),
          eventKey = Some(tophEvent.eventKey),
          hypocentreKey = tophEvent.hypocentreKey,
          magnitudeKey = tophEvent.magnitudeKey
        )

        for
          channel <- GRPC.createChannel
          client <- SpatialServiceClient.scoped(channel)
          _ <- SweetMockitoLayer[TophSpatialServiceClient]
            .whenF2(_.searchEvent(expectedTophEventQuery))
            .thenReturn(tophEvent)

          result <- client.searchEvent(expectedQuery).runCollect
        yield assertTrue(
          result == Seq(expectedEvent)
        )
      }
    ).provideSome[Scope](
      GRPC.serverLayerFor[ZSpatialService[RequestContext]],
      ZLayer(SpatialService()),
      SweetMockitoLayer.newMockLayer[TophSpatialServiceClient]
    )
