package toph.converter

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import quakeml.QuakeMLMagnitude
import toph.model.data.MagnitudeData
import zio.Task
import zio.ZIO

object MagnitudeDataConverter:

  def fromQMagnitude(magnitude: QuakeMLMagnitude): Task[MagnitudeData] = ZIO.attempt {
    magnitude
      .into[MagnitudeData]
      .transform(Field.const(_.key, magnitude.publicID: String))
  }
