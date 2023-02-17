package toph.converter

import io.github.arainko.ducktape.Transformer
import org.locationtech.jts.geom.CoordinateSequence
import quakeml.Comment
import quakeml.EvaluationMode
import quakeml.EvaluationStatus
import quakeml.RealQuantity
import quakeml.ResourceReference
import quakeml.TimeQuantity
import toph.geom.CoordinateSequenceFactory

import java.time.Clock
import java.time.ZonedDateTime

val ZoneId = Clock.systemUTC().getZone()

given Transformer[ResourceReference, String] = _.uri

given Transformer[TimeQuantity, ZonedDateTime] = _.value

given Transformer[RealQuantity, Double] = _.value

given Transformer[EvaluationMode, String] = _.value

given Transformer[EvaluationStatus, String] = _.value

given Transformer[String, ZonedDateTime] =
  ZonedDateTime
    .parse(_)
    .withZoneSameInstant(ZoneId)

given Transformer[ZonedDateTime, String] = _.toString()

given seqDoubleToCoordinateSequence: Transformer[Seq[Double], CoordinateSequence] = values =>
  if values.size % 2 == 0 then
    val sequence = CoordinateSequenceFactory.create(values.size / 2, 2)
    for (coordinate, idx) <- values.sliding(2, 2).zipWithIndex do
      sequence.setOrdinate(idx, 0, coordinate(0))
      sequence.setOrdinate(idx, 1, coordinate(1))
    sequence
  else throw IllegalArgumentException("Invalid sequence!")

given [A, B](using tranformer: Transformer[A, B]): Conversion[A, B] =
  tranformer.transform(_)

given [A, B](using transformer: Transformer[A, B]): Conversion[Option[A], Option[B]] =
  _.map(transformer.transform)

given Transformer[Comment, String] = _.text
