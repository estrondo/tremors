package graboid.http

import graboid.BuildInfo
import zio.RIO
import zio.Scope
import zio.http.Client
import zio.http.Header
import zio.http.Header.UserAgent.ProductOrComment
import zio.http.Request
import zio.http.Response

object HttpClient:

  def request(request: Request): RIO[Client & Scope, Response] =
    Client.streaming(
      request.addHeader(
        Header.UserAgent(ProductOrComment.Product(BuildInfo.name, Some(BuildInfo.version))),
      ),
    )
