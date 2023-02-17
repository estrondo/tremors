package toph.query.spatial

import org.locationtech.jts.geom.CoordinateSequence
import org.locationtech.jts.geom.Geometry
import toph.geom.Factory
import toph.geom.isRing

def toQueriableGeometry(sequence: CoordinateSequence): Geometry =
  if sequence.size() == 1 then Factory.createPoint(sequence)
  else if sequence.isRing then Factory.createPolygon(sequence)
  else Factory.createLineString(sequence)
