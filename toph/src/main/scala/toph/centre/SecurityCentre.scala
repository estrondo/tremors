package toph.centre

import io.grpc.StatusException
import scalapb.zio_grpc.RequestContext
import toph.model.AuthenticatedUser
import zio.IO

trait SecurityCentre:

  def authenticate(request: RequestContext): IO[StatusException, AuthenticatedUser]
