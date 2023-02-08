package toph.converter

import io.github.arainko.ducktape.Transformer
import quakeml.Comment
import quakeml.ResourceReference
import quakeml.TimeQuantity

import java.time.ZonedDateTime

given Transformer[ResourceReference, String] = _.uri

given Transformer[TimeQuantity, ZonedDateTime] = _.value

given [A, B](using tranformer: Transformer[A, B]): Conversion[A, B] =
  tranformer.transform(_)

given [A, B](using transformer: Transformer[A, B]): Conversion[Option[A], Option[B]] =
  _.map(transformer.transform)

given Transformer[Comment, String] = _.text
