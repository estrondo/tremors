package toph.query

import org.locationtech.jts.geom.CoordinateSequence
import org.locationtech.jts.geom.Geometry
import toph.geom.GeometryFactory
import toph.geom.isRing

def toQueriableGeometry(sequence: CoordinateSequence): Geometry =
  if sequence.size() == 1 then GeometryFactory.createPoint(sequence)
  else if sequence.isRing then GeometryFactory.createPolygon(sequence)
  else GeometryFactory.createLineString(sequence)
