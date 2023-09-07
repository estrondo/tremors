package graboid.http

import zio.Task
import zio.http.QueryParams

trait UpdateQueryParam[-T]:

  def apply(value: T, queryParams: QueryParams): Task[Seq[QueryParams]] =
    for allParams <- getParams(value) yield for params <- allParams yield params.foldLeft(queryParams) {
      case (queryParams, (key, value)) =>
        queryParams.add(key, value)
    }

  def getParams(value: T): Task[Seq[Iterable[(String, String)]]]

object UpdateQueryParam:

  def apply[Q: UpdateQueryParam](query: Q, queryParams: QueryParams): Task[Seq[QueryParams]] =
    summon[UpdateQueryParam[Q]](query, queryParams)
