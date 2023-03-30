package webapi.geom

import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory

val CoordinateSequenceFactory = PackedCoordinateSequenceFactory.DOUBLE_FACTORY

val GeometryFactory = new GeometryFactory(PrecisionModel(PrecisionModel.FLOATING), 4326, CoordinateSequenceFactory)
