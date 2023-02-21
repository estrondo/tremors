package toph.geom

import org.locationtech.jts.geom.CoordinateSequence
import org.locationtech.jts.geom.CoordinateSequenceFactory
import org.locationtech.jts.geom.CoordinateSequences
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory
import toph.converter.seqDoubleToCoordinateSequence

val CoordinateSequenceFactory = PackedCoordinateSequenceFactory.DOUBLE_FACTORY

val GeometryFactory = new GeometryFactory(PrecisionModel(PrecisionModel.FLOATING), 4326, CoordinateSequenceFactory)

extension (factory: CoordinateSequenceFactory)
  def create(values: Double*): CoordinateSequence =
    seqDoubleToCoordinateSequence.transform(values)

extension (sequence: CoordinateSequence) def isRing: Boolean = CoordinateSequences.isRing(sequence)
