package toph.service

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import scalapb.zio_grpc.RequestContext
import testkit.core.createZonedDateTime
import toph.GRPC
import toph.IT
import toph.converter.SpatialQueryConverter
import toph.fixture.EpicentreFixture
import toph.fixture.HypocentreFixture
import toph.grpc.spatial.EpicentreQuery
import toph.grpc.spatial.GRPCEpicentre
import toph.grpc.spatial.GRPCHypocentre
import toph.grpc.spatial.HypocentreQuery
import toph.grpc.spatial.ZioSpatial.SpatialServiceClient
import toph.grpc.spatial.ZioSpatial.ZSpatialService
import toph.manager.SpatialManager
import zio.Chunk
import zio.Schedule
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.durationInt
import zio.test.Spec
import zio.test.TestEnvironment
import zio.test.assertTrue

object SpatialServiceIT extends IT:

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("A SpatialService")(
      test("It should query for Epicentres.") {
        val expectedQuery = EpicentreQuery(
          boundary = Seq(-45, -21),
          boundaryRadius = Some(150000),
          startTime = Some(createZonedDateTime().toString()),
          endTime = Some(createZonedDateTime().plusDays(5).toString())
        )

        val epicentre = EpicentreFixture.createRandom()

        ZIO.scoped(
          for
            channel      <- GRPC.channel
            client       <- SpatialServiceClient.scoped(channel)
            spatialQuery <- SpatialQueryConverter.from(expectedQuery)
            _            <- SweetMockitoLayer[SpatialManager]
                              .whenF2(_.search(spatialQuery))
                              .thenReturn(epicentre)
            result       <- client.searchEpicentre(expectedQuery).runCollect
          yield assertTrue(
            result == Chunk(
              GRPCEpicentre(
                key = epicentre.key,
                lng = epicentre.position.lng,
                lat = epicentre.position.lat,
                magnitude = 0.0,
                magnitudeType = "",
                time = epicentre.time.toString()
              )
            )
          )
        )
      },
      test("It should query for Hypocentres.") {
        val expectedQuery = HypocentreQuery(
          boundary = Seq(-45, -21),
          boundaryRadius = Some(150000),
          startTime = Some(createZonedDateTime().toString()),
          endTime = Some(createZonedDateTime().plusDays(5).toString())
        )

        val hypocentre = HypocentreFixture.createRandom()

        ZIO.scoped(
          for
            channel      <- GRPC.channel
            client       <- SpatialServiceClient.scoped(channel)
            spatialQuery <- SpatialQueryConverter.from(expectedQuery)
            _            <- SweetMockitoLayer[SpatialManager]
                              .whenF2(_.search(spatialQuery))
                              .thenReturn(hypocentre)
            result       <- client.searchHypocentre(expectedQuery).runCollect
          yield assertTrue(
            result == Chunk(
              GRPCHypocentre(
                key = hypocentre.key,
                lng = hypocentre.position.lng,
                lat = hypocentre.position.lat,
                depth = hypocentre.position.z,
                magnitude = 0.0,
                magnitudeType = "",
                time = hypocentre.time.toString()
              )
            )
          )
        )
      }
    ).provideSome[Scope](
      serviceLayer,
      GRPC.serverLayerFor[ZSpatialService[RequestContext]]
    )

  private val serviceLayer =
    SweetMockitoLayer.newMockLayer[SpatialManager] >+> ZLayer(ZIO.serviceWith[SpatialManager](SpatialService.apply))
