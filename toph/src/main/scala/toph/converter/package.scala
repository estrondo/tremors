package toph.converter

import grpc.toph.spatial.{CreationInfo => GRPCCreationInfo}
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.Transformer
import io.github.arainko.ducktape.into
import org.locationtech.jts.geom.CoordinateSequence
import quakeml.QuakeMLComment
import quakeml.QuakeMLEvaluationMode
import quakeml.QuakeMLEvaluationStatus
import quakeml.QuakeMLRealQuantity
import quakeml.QuakeMLResourceReference
import quakeml.QuakeMLTimeQuantity
import scalapb.UnknownFieldSet
import toph.geom.CoordinateSequenceFactory
import toph.model.data.CreationInfoData

import java.time.Clock
import java.time.ZonedDateTime

val ZoneId = Clock.systemUTC().getZone()

given Transformer[QuakeMLResourceReference, String] = _.uri

given Transformer[QuakeMLTimeQuantity, ZonedDateTime] = _.value

given Transformer[QuakeMLRealQuantity, Double] = _.value

given Transformer[QuakeMLEvaluationMode, String] = _.value

given Transformer[QuakeMLEvaluationStatus, String] = _.value

given Transformer[String, ZonedDateTime] =
  ZonedDateTime
    .parse(_)
    .withZoneSameInstant(ZoneId)

given Transformer[ZonedDateTime, String] = _.toString()

given Transformer[CreationInfoData, GRPCCreationInfo] = input =>
  input
    .into[GRPCCreationInfo]
    .transform(
      Field.const(_.unknownFields, UnknownFieldSet.empty)
    )

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

given [A, B](using transformer: Transformer[A, Seq[B]]): Transformer[Option[A], Seq[B]] = {
  case None        => Seq.empty
  case Some(value) => transformer.transform(value)
}

given Transformer[QuakeMLComment, String] = _.text
