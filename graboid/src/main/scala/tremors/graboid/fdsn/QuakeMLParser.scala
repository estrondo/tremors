package tremors.graboid.fdsn

import zio.Chunk
import tremors.graboid.Crawler

trait QuakeMLParser:

  def evaluate(buffer: Seq[Byte]): Option[Crawler.Info]
