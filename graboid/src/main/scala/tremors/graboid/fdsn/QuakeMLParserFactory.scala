package tremors.graboid.fdsn

import zio.UIO

trait QuakeMLParserFactory:

  def apply(): QuakeMLParser