package graboid.quakeml

import zio.stream.ZStream
import zio.Task
import graboid.Crawler

trait QuakeMLParser:

  def parse(stream: ZStream[Any, Throwable, Byte]): Task[Crawler.Stream]

object QuakeMLParser:

  def apply(): QuakeMLParser = QuakeMLParserImpl()
