package toph.converter

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import quakeml.{Origin => QOrigin}
import toph.model.Hypocentre
import toph.model.Point3D
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
        Field.const(_.position, Point3D.from(origin.longitude, origin.latitude, depth))
      )
  }
