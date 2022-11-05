package webapi1x.handler

import zhttp.http.Request

import zio.Task
import zhttp.http.Response
import zio.ZIO
import zio.json.*
import webapi1x.BuildInfo

class AboutHandler:

  case class About(
      name: String,
      version: String
  )

  given JsonEncoder[About] = DeriveJsonEncoder.gen

  def apply(request: Request): Task[Response] = ZIO.attempt {
    Response.json(
      About(
        name = BuildInfo.name,
        version = BuildInfo.version
      ).toJson
    )
  }
