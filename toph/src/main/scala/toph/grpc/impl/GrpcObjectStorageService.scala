package toph.grpc.impl

import com.softwaremill.macwire.wire
import com.softwaremill.tagging.@@
import io.grpc.Status
import io.grpc.StatusException
import toph.context.TophExecutionContext
import toph.security.AccessToken
import toph.service.ObjectStorageReadService
import toph.service.ObjectStorageUpdateService
import toph.v1.grpc.GrpcObject
import toph.v1.grpc.GrpcObject.Request.Content
import toph.v1.grpc.ZioGrpc
import zio.Cause
import zio.Exit
import zio.ZIO
import zio.stream

object GrpcObjectStorageService:

  def apply(
      systemReadService: ObjectStorageReadService @@ SystemTag,
      userReadService: ObjectStorageReadService @@ UserTag,
      userWriteService: ObjectStorageUpdateService @@ UserTag,
  ): ZioGrpc.ZObjectStorageService[AccessToken] =
    wire[Impl]

  trait SystemTag

  trait UserTag

  class Impl(
      systemReadService: ObjectStorageReadService @@ SystemTag,
      userReadService: ObjectStorageReadService @@ UserTag,
      userUpdateService: ObjectStorageUpdateService @@ UserTag,
  ) extends ZioGrpc.ZObjectStorageService[AccessToken]:

    override def system(
        request: stream.Stream[StatusException, GrpcObject.ReadRequest],
        context: AccessToken,
    ): stream.Stream[StatusException, GrpcObject.Response] =
      given TophExecutionContext = TophExecutionContext.account(context.account)
      request
        .mapZIO { request =>
          systemReadService[GrpcObject.Response](request)
        }
        .tapErrorCause(reportError)
        .mapError(convertError)

    override def user(
        request: stream.Stream[StatusException, GrpcObject.Request],
        context: AccessToken,
    ): stream.Stream[StatusException, GrpcObject.Response] =
      request
        .mapZIO { request =>
          given TophExecutionContext = TophExecutionContext.account(context.account)
          request.content match
            case Content.ReadRequest(request)   =>
              userReadService[GrpcObject.Response](request)
            case Content.UpdateRequest(request) =>
              userUpdateService[GrpcObject.Response](request)
            case Content.Empty                  =>
              Exit.succeed(GrpcObject.Response())
        }
        .tapErrorCause(reportError)
        .mapError(convertError)

    private def reportError(cause: Cause[Throwable]) =
      ZIO.logErrorCause("Unable to perform request!", cause)

    private def convertError(cause: Throwable) =
      StatusException(Status.UNKNOWN)
