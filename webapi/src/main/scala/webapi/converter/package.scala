package webapi.converter

import io.github.arainko.ducktape.Transformer
import grpc.webapi.spatial.{CreationInfo => GRPCCreationInfo}
import grpc.toph.spatial.{CreationInfo => TophGRPCCreationInfo}
import io.github.arainko.ducktape.{into, Field}

given Transformer[TophGRPCCreationInfo, GRPCCreationInfo] = input =>
  input
    .into[GRPCCreationInfo]
    .transform()
