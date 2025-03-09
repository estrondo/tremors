package toph.model.objectstorage

import io.github.arainko.ducktape.*
import toph.v1.grpc.GrpcObject
import zio.Task
import zio.ZIO

case class ObjectStorageResponse(
    id: String,
    results: Seq[ObjectStorageResult],
)

object ObjectStorageResponse:

  trait To[A]:
    def apply(result: ObjectStorageResponse): Task[A]

  given toGrpcResponse: To[GrpcObject.Response] with

    given Transformer[Item, GrpcObject.Item] =
      _.to[GrpcObject.Item]

    override def apply(response: ObjectStorageResponse): Task[GrpcObject.Response] = ZIO.attempt {
      GrpcObject.Response(
        id = response.id,
        results = response.results.map(toResult),
      )
    }

    private def toResult(result: ObjectStorageResult): GrpcObject.Result =
      result match
        case result: Error              =>
          GrpcObject.Result(
            content =
              GrpcObject.Result.Content.Error(result.into[GrpcObject.Error].transform(Field.default(_.unknownFields))),
          )
        case result: FetchObjectResult  =>
          GrpcObject.Result(
            content = GrpcObject.Result.Content.FetchObject(
              result.into[GrpcObject.FetchObjectResult].transform(Field.default(_.unknownFields)),
            ),
          )
        case result: FetchFolderResult  =>
          GrpcObject.Result(
            content = GrpcObject.Result.Content.FetchFolder(
              result.into[GrpcObject.FetchFolderResult].transform(Field.default(_.unknownFields)),
            ),
          )
        case result: UpdateObjectResult =>
          GrpcObject.Result(
            content = GrpcObject.Result.Content.UpdateObject(
              result.into[GrpcObject.UpdateObjectResult].transform(Field.default(_.unknownFields)),
            ),
          )
        case result: RemoveObjectResult =>
          GrpcObject.Result(
            content = GrpcObject.Result.Content.RemoveObject(
              result.into[GrpcObject.RemoveObjectResult].transform(Field.default(_.unknownFields)),
            ),
          )
        case result: RemoveFolderResult =>
          GrpcObject.Result(
            content = GrpcObject.Result.Content.RemoveFolder(
              result.into[GrpcObject.RemoveFolderResult].transform(Field.default(_.unknownFields)),
            ),
          )
