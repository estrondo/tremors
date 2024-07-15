package toph.model

import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel

val geometryFactory = GeometryFactory(PrecisionModel(PrecisionModel.FLOATING), 4326)
