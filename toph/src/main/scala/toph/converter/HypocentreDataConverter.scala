package toph.converter

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import org.locationtech.jts.geom.Coordinate
import quakeml.QuakeMLOrigin
import quakeml.QuakeMLRealQuantity
import toph.geom.GeometryFactory
import toph.model.Uncertainty2D
import toph.model.data.HypocentreData
import zio.Task
import zio.ZIO

object HypocentreDataConverter:

  def from(origin: QuakeMLOrigin): Task[HypocentreData] = ZIO.attempt {
    origin
      .into[HypocentreData]
      .transform(
        Field.const(_.positionUncertainty, Uncertainty2D.from(origin.longitude, origin.latitude)),
        Field.const(_.key, origin.publicID: String),
        Field.const(_.timeUncertainty, origin.time.uncertainty.getOrElse(0d).toInt),
        Field.const(_.position, GeometryFactory.createPoint(Coordinate(origin.longitude.value, origin.latitude.value))),
        Field.const(_.depth, origin.depth.map(_.value)),
        Field.const(_.depthUncertainty, origin.depth.flatMap(_.uncertainty))
      )
  }
