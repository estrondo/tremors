package toph.converter

import io.github.arainko.ducktape.Transformer
import quakeml.Comment
import quakeml.EvaluationMode
import quakeml.EvaluationStatus
import quakeml.RealQuantity
import quakeml.ResourceReference
import quakeml.TimeQuantity

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

given [A, B](using tranformer: Transformer[A, B]): Conversion[A, B] =
  tranformer.transform(_)

given [A, B](using transformer: Transformer[A, B]): Conversion[Option[A], Option[B]] =
  _.map(transformer.transform)

given Transformer[Comment, String] = _.text
