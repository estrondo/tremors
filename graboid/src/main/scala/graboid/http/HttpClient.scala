package graboid.http

import graboid.BuildInfo
import zio.RIO
import zio.Scope
import zio.http.Client
import zio.http.Header
import zio.http.Request
import zio.http.Response

object HttpClient:

  def request(request: Request): RIO[Client & Scope, Response] =
    Client.request(
      request.addHeader(Header.UserAgent.Product(BuildInfo.name, Some(BuildInfo.version))),
    )
