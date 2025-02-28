package toph.model.objectstorage

import io.github.arainko.ducktape.*
import scala.language.future
import scala.reflect.ClassTag
import toph.TophException
import toph.v1.grpc.GrpcObject
import toph.v1.grpc.GrpcObject.ReadOperation.Content
import toph.v1.grpc.GrpcObject.UpdateOperation.Content
import zio.Task
import zio.ZIO

sealed trait ObjectStorageRequest

private inline def extractAndTransform[A, B](o: Option[A])(using ClassTag[A], Transformer[A, B]): B =
  o match
    case Some(value) =>
      Transformer[A, B].transform(value)
    case None        =>
      throw TophException.ObjectStorage(s"Unable to retrieve ${summon[ClassTag[A]].runtimeClass.getSimpleName}!")

case class ObjectStorageReadRequest(
    id: String,
    operations: Seq[ObjectStorageReadOperation],
) extends ObjectStorageRequest

object ObjectStorageReadRequest:

  trait From[A]:
    def apply(a: A): Task[ObjectStorageReadRequest]

  given fromGrpcRead: From[GrpcObject.ReadRequest] with

    override def apply(request: GrpcObject.ReadRequest): Task[ObjectStorageReadRequest] = ZIO.attempt {
      ObjectStorageReadRequest(
        id = request.id,
        operations = request.operations.map(fromOperation),
      )
    }

    private def fromOperation(operation: GrpcObject.ReadOperation) =
      operation.content match
        case GrpcObject.ReadOperation.Content.FetchObject(operation) =>
          operation.to[FetchObjectOperation]
        case GrpcObject.ReadOperation.Content.FetchFolder(operation) =>
          operation.to[FetchFolderOperation]
        case GrpcObject.ReadOperation.Content.Empty                  =>
          EmptyReadOperation

case class ObjectStorageUpdateRequest(
    id: String,
    operations: Seq[ObjectStorageUpdateOperation],
) extends ObjectStorageRequest

object ObjectStorageUpdateRequest:

  trait From[A]:
    def apply(a: A): Task[ObjectStorageUpdateRequest]

  given fromGrpcUpdate: From[GrpcObject.UpdateRequest] with

    override def apply(request: GrpcObject.UpdateRequest): Task[ObjectStorageUpdateRequest] = ZIO.attempt {
      ObjectStorageUpdateRequest(
        id = request.id,
        operations = request.operations.map(fromOperation),
      )
    }

    private def fromOperation(operation: GrpcObject.UpdateOperation) =
      operation.content match
        case GrpcObject.UpdateOperation.Content.UpdateObject(operation) =>
          operation.to[UpdateObjectOperation]
        case GrpcObject.UpdateOperation.Content.RemoveObject(operation) =>
          operation.to[RemoveObjectOperation]
        case GrpcObject.UpdateOperation.Content.RemoveFolder(operation) =>
          operation.to[RemoveFolderOperation]
        case GrpcObject.UpdateOperation.Content.Empty                   =>
          EmptyUpdateOperation
