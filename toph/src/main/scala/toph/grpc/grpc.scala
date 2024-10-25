package toph.grpc

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import scalapb.UnknownFieldSet
import toph.model.Account
import toph.grpc.User
import zio.Task
import zio.ZIO

object UserTransformer:

  def from(user: Account): Task[User] = ZIO.attempt {
    user
      .into[User]
      .transform(Field.const(_.unknownFields, UnknownFieldSet.empty))
  }
