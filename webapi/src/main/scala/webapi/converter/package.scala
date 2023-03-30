package webapi.converter

import grpc.toph.spatial.{CreationInfo => TophGRPCCreationInfo}
import grpc.webapi.spatial.{CreationInfo => GRPCCreationInfo}
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.Transformer
import io.github.arainko.ducktape.into

given Transformer[TophGRPCCreationInfo, GRPCCreationInfo] = input =>
  input
    .into[GRPCCreationInfo]
    .transform()
