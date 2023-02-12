package toph.converter

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import quakeml.{Magnitude => QMagnitude}
import toph.model.Magnitude
import zio.Task
import zio.ZIO

object MagnitudeConverter:

  def fromQMagnitude(magnitude: QMagnitude): Task[Magnitude] = ZIO.attempt {
    magnitude
      .into[Magnitude]
      .transform(Field.const(_.key, magnitude.publicID: String))
  }
