package webapi.geom

import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory
import org.locationtech.jts.geom.PrecisionModel

val CoordinateSequenceFactory = PackedCoordinateSequenceFactory.DOUBLE_FACTORY

val GeometryFactory = new GeometryFactory(PrecisionModel(PrecisionModel.FLOATING), 4326, CoordinateSequenceFactory)
