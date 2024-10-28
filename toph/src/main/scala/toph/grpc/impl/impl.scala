package toph.grpc.impl

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import scalapb.UnknownFieldSet
import toph.grpc.GRPCAccount
import toph.model.Account
import zio.Task
import zio.ZIO

//noinspection ScalaFileName
object GRPCAccountTransformer:

  def from(account: Account): Task[GRPCAccount] = ZIO.attempt {
    account
      .into[GRPCAccount]
      .transform(Field.const(_.unknownFields, UnknownFieldSet.empty))
  }
