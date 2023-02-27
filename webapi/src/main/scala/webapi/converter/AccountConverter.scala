package webapi.converter

import grpc.webapi.account.{Account => GRPCAccount}
import webapi.model.Account
import zio.{ZIO, Task}
import io.github.arainko.ducktape.{into, Field}
import zio.Clock
import webapi.currentZonedDateTime
import java.time.ZonedDateTime

object AccountConverter:

  def from(input: GRPCAccount): Task[Account] =
    for
      zonedDateTime <- Clock.currentZonedDateTime()
      converted     <- from(input, zonedDateTime)
    yield converted

  def from(input: GRPCAccount, createdAt: ZonedDateTime): Task[Account] = ZIO.attempt {
    input
      .into[Account]
      .transform(
        Field.const(_.createdAt, createdAt)
      )
  }
