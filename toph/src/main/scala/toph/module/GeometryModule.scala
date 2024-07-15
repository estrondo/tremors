package toph.module

import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import zio.Task
import zio.ZIO

class GeometryModule(val geometryFactory: GeometryFactory)

object GeometryModule:

  def apply(srid: Int): Task[GeometryModule] =
    ZIO.succeed(new GeometryModule(GeometryFactory(PrecisionModel(PrecisionModel.FLOATING), srid)))
