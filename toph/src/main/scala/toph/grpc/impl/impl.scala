package toph.grpc.impl

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import scalapb.UnknownFieldSet
import toph.grpc.GRPCAccount
import toph.model.Account
import zio.Task
import zio.ZIO

object UserTransformer:

  def from(user: Account): Task[GRPCAccount] = ZIO.attempt {
    user
      .into[GRPCAccount]
      .transform(Field.const(_.unknownFields, UnknownFieldSet.empty))
  }
