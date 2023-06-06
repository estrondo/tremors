package webapi.converter

import toph.grpc.{CreationInfo => TophGRPCCreationInfo}
import webapi.grpc.{CreationInfo => GRPCCreationInfo}
import io.github.arainko.ducktape.Transformer
import io.github.arainko.ducktape.into

given Transformer[TophGRPCCreationInfo, GRPCCreationInfo] = input =>
  input
    .into[GRPCCreationInfo]
    .transform()
