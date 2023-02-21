package toph.service

import grpc.toph.spatial.CreationInfo
import grpc.toph.spatial.Event
import grpc.toph.spatial.EventQuery
import grpc.toph.spatial.ZioSpatial.SpatialServiceClient
import grpc.toph.spatial.ZioSpatial.ZSpatialService
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers.any
import scalapb.zio_grpc.RequestContext
import teskit.zio.grpc.GRPC
import toph.IT
import toph.fixture.EventFixture
import toph.manager.SpatialManager
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.assertTrue

object SpatialServiceIT extends IT:

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("A SpatialService")(
      test("It should query for Events.") {
        val queriableEvent = EventFixture.createRandom()

        val request = EventQuery()
        val event   = Event(
          key = queriableEvent.key,
          eventKey = queriableEvent.eventKey,
          hypocentreKey = queriableEvent.hypocentreKey,
          magnitudeKey = queriableEvent.magnitudeKey,
          eventType = queriableEvent.eventType,
          position = Seq(queriableEvent.position.get.getX(), queriableEvent.position.get.getY()),
          positionUncertainty = queriableEvent.positionUncertainty.foldLeft(Seq.empty) { (_, p) =>
            Seq(p.lng, p.lat)
          },
          depth = queriableEvent.depth,
          depthUncertainty = queriableEvent.depthUncertainty,
          time = queriableEvent.time.map(_.toString()),
          timeUncertainty = queriableEvent.timeUncertainty,
          stationCount = queriableEvent.stationCount,
          magnitude = queriableEvent.magnitude,
          magnitudeType = queriableEvent.magnitudeType,
          creationInfo = queriableEvent.creationInfo.map(x =>
            CreationInfo(
              agencyID = x.agencyID,
              agencyURI = x.agencyURI,
              author = x.author,
              creationTime = x.creationTime.map(_.toString()),
              version = x.version
            )
          )
        )

        for
          _       <- SweetMockitoLayer[SpatialManager]
                       .whenF2(_.search(any()))
                       .thenReturn(queriableEvent)
          channel <- GRPC.createChannel
          client  <- SpatialServiceClient.scoped(channel)
          result  <- client.searchEvent(request).runCollect
        yield assertTrue(
          result == Seq(event)
        )
      }
    ).provideSome[Scope](
      SweetMockitoLayer.newMockLayer[SpatialManager],
      ZLayer(ZIO.serviceWith[SpatialManager](SpatialService.apply)),
      GRPC.serverLayerFor[ZSpatialService[RequestContext]]
    ) @@ TestAspect.sequential
