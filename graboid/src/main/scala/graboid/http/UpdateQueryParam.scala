package graboid.http

import scala.util.Try
import zio.http.QueryParams

trait UpdateQueryParam[-T]:

  def apply(value: T, queryParams: QueryParams): Try[QueryParams] =
    for params <- getParams(value) yield params.foldLeft(queryParams) { case (newQueryParams, (key, value)) =>
      newQueryParams.add(key, value)
    }

  def getParams(value: T): Try[Iterable[(String, String)]]
