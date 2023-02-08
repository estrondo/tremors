package toph.converter

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import quakeml.{Origin => QOrigin}
import toph.model.Epicentre
import toph.model.Point2D
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
        Field.const(_.timeUncertainty, origin.time.uncertainty getOrElse 0),
        Field.const(_.position, Point2D.from(origin.longitude, origin.latitude))
      )
  }
