package toph.converter

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import org.locationtech.jts.geom.Coordinate
import quakeml.{Origin => QOrigin}
import toph.geom.Factory
import toph.model.Hypocentre
import toph.model.Uncertainty3D
import zio.Task
import zio.ZIO

object HypocentreConverter:

  def from(origin: QOrigin): Task[Option[Hypocentre]] = ZIO.attempt {
    for depth <- origin.depth
    yield origin
      .into[Hypocentre]
      .transform(
        Field.const(_.positionUncertainty, Uncertainty3D.from(origin.longitude, origin.latitude, depth)),
        Field.const(_.key, origin.publicID: String),
        Field.const(_.timeUncertainty, origin.time.uncertainty.getOrElse(0d).toInt),
        Field.const(_.position, Factory.createPoint(Coordinate(origin.longitude.value, origin.latitude.value))),
        Field.const(_.depth, depth.value)
      )
  }
