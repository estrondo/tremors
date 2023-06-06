package webapi.converter

import core.KeyGenerator
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import java.time.ZonedDateTime
import webapi.currentZonedDateTime
import webapi.grpc.{Account => GRPCAccount}
import webapi.model.Account
import zio.Clock
import zio.Task
import zio.ZIO

object AccountConverter:

  def from(input: GRPCAccount, keyGenerator: KeyGenerator): Task[Account] =
    for
      zonedDateTime <- Clock.currentZonedDateTime()
      converted     <- from(input, keyGenerator, zonedDateTime)
    yield converted

  def from(input: GRPCAccount, keyGenerator: KeyGenerator, createdAt: ZonedDateTime): Task[Account] = ZIO.attempt {
    input
      .into[Account]
      .transform(
        Field.const(_.createdAt, createdAt),
        Field.const(_.secret, keyGenerator.next4()),
        Field.const(_.active, false)
      )
  }
