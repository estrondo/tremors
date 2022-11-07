package webapi1x.handler

import com.softwaremill.macwire.wire
import graboid.protocol.AddCrawler
import webapi1x.Marshalling.extractJSON
import webapi1x.graboid.CrawlerDescriptorMapper
import webapi1x.graboid.GraboidCommandDispatcher
import webapi1x.graboid.UpdateCrawlerDescriptorMapper
import webapi1x.toZIO
import zhttp.http.Request
import zhttp.http.Response
import zio.Task
import zio.json.DeriveJsonDecoder
import zio.json.JsonDecoder

import java.time.Duration
import java.time.ZonedDateTime

import GraboidHandler.*

trait GraboidHandler:

  def createCrawler(request: Request): Task[Response]

  def getInfoFromAll(request: Request): Task[Response]

  def getInfo(key: String, request: Request): Task[Response]

  def update(key: String, request: Request): Task[Response]

  def delete(key: String, request: Request): Task[Response]

object GraboidHandler:

  def apply(
      manager: GraboidCommandDispatcher
  ): GraboidHandler = wire[GraboidHandlerImpl]

  case class CreateCrawlerRequest(
      id: String,
      name: String,
      `type`: String,
      source: String,
      windowDuration: Duration,
      starting: ZonedDateTime
  )

  case class UpdateCrawlerRequest(
      name: Option[String],
      `type`: Option[String],
      source: Option[String],
      windowDuration: Option[Duration],
      starting: Option[ZonedDateTime]
  )

  case class CrawlerCreated(
  )

  given JsonDecoder[CreateCrawlerRequest] = DeriveJsonDecoder.gen

  given JsonDecoder[UpdateCrawlerRequest] = DeriveJsonDecoder.gen

private class GraboidHandlerImpl(
    manager: GraboidCommandDispatcher
) extends GraboidHandler:

  override def createCrawler(request: Request): Task[Response] =
    for
      createCrawler <- extractJSON[CreateCrawlerRequest](request)
      descriptor    <- CrawlerDescriptorMapper.from(createCrawler).toZIO()
      _             <- manager.dispatch(AddCrawler(descriptor))
    yield Response.text("as")

  override def getInfo(key: String, request: Request): Task[Response] = ???

  override def getInfoFromAll(request: Request): Task[Response] = ???

  override def delete(key: String, request: Request): Task[Response] = ???

  override def update(key: String, request: Request): Task[Response] =
    for
      updateCrawler <- extractJSON[UpdateCrawlerRequest](request)
      descriptor    <- UpdateCrawlerDescriptorMapper.from(updateCrawler).toZIO()
    yield ???
