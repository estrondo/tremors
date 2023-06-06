package webapi.converter

import webapi.grpc.{Account => GRPCAccount}
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import scalapb.UnknownFieldSet
import webapi.model.Account
import zio.Task
import zio.ZIO

object GRPCAccountConverter:

  def from(input: Account): Task[GRPCAccount] = ZIO.attempt {
    input
      .into[GRPCAccount]
      .transform(
        Field.const(_.unknownFields, UnknownFieldSet.empty)
      )
  }
