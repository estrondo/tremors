package toph.model

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.quakeml.Event
import zio.RIO
import zio.ZIO

object TophEventMapper:

  def apply(event: Event): RIO[KeyGenerator, TophEvent] =
    ZIO.serviceWith[KeyGenerator] { keyGenerator =>
      event
        .into[TophEvent]
        .transform(
          Field.const(_.id, keyGenerator.generate(KeyLength.Medium))
        )
    }
