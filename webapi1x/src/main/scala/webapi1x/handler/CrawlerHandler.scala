package webapi1x.handler

import com.softwaremill.macwire.wire
import webapi1x.Marshalling.extractJSON
import webapi1x.crawler.CrawlerManager
import webapi1x.graboid.CrawlerDescriptorMapper
import webapi1x.toZIO
import zhttp.http.Request
import zhttp.http.Response
import zio.Task
import zio.json.DeriveJsonDecoder
import zio.json.JsonDecoder

import java.time.Duration
import java.time.ZonedDateTime

import CrawlerHandler.CreateCrawler

trait CrawlerHandler:

  def createCrawler(request: Request): Task[Response]

  def getInfoFromAll(request: Request): Task[Response]

  def getInfo(key: String, request: Request): Task[Response]

  def update(key: String, request: Request): Task[Response]

  def delete(key: String, request: Request): Task[Response]

object CrawlerHandler:

  def apply(
      manager: CrawlerManager
  ): CrawlerHandler = wire[CrawlerHandlerImpl]

  case class CreateCrawler(
      id: String,
      name: String,
      `type`: String,
      source: String,
      windowDuration: Duration,
      starting: ZonedDateTime
  )

  case class CrawlerCreated(
  )

  given JsonDecoder[CreateCrawler] = DeriveJsonDecoder.gen

private class CrawlerHandlerImpl(
    manager: CrawlerManager
) extends CrawlerHandler:

  override def createCrawler(request: Request): Task[Response] =
    for
      createCrawler <- extractJSON[CreateCrawler](request)
      descriptor    <- CrawlerDescriptorMapper.from(createCrawler).toZIO()
      _             <- manager.create(descriptor)
    yield Response.text("as")

  override def getInfo(key: String, request: Request): Task[Response] = ???

  override def getInfoFromAll(request: Request): Task[Response] = ???

  override def delete(key: String, request: Request): Task[Response] = ???

  override def update(key: String, request: Request): Task[Response] = ???
