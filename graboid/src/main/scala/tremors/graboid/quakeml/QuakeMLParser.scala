package tremors.graboid.quakeml

import zio.Chunk
import tremors.graboid.Crawler
import tremors.graboid.Crawler.Info

trait QuakeMLParser:

  def evaluate(buffer: Seq[Byte]): Option[Crawler.Info]

object QuakeMLParser:
  def apply(): QuakeMLParser = QuakeMLParserImpl()

private class QuakeMLParserImpl extends QuakeMLParser:
  override def evaluate(buffer: Seq[Byte]): Option[Info] = ???
