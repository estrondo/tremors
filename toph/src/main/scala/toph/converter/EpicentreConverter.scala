package toph.converter

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import org.locationtech.jts.geom.Coordinate
import quakeml.{Origin => QOrigin}
import toph.geom.Factory
import toph.model.Epicentre
import toph.model.Uncertainty2D
import zio.Task
import zio.ZIO

object EpicentreConverter:

  def from(origin: QOrigin): Task[Epicentre] = ZIO.attempt {
    origin
      .into[Epicentre]
      .transform(
        Field.const(_.key, origin.publicID: String),
        Field.const(_.positionUncertainty, Uncertainty2D.from(origin.longitude, origin.latitude)),
        Field.const(_.timeUncertainty, origin.time.uncertainty.getOrElse(0d).toInt),
        Field.const(_.position, Factory.createPoint(Coordinate(origin.longitude.value, origin.latitude.value)))
      )
  }
