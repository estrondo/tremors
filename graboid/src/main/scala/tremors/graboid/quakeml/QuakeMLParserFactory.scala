package tremors.graboid.quakeml

import zio.UIO

trait QuakeMLParserFactory:

  def apply(): QuakeMLParser

object QuakeMLParserFactory:

  def apply(): QuakeMLParserFactory = QuakeMLParserFactoryImpl()

private class QuakeMLParserFactoryImpl extends QuakeMLParserFactory:
  def apply(): QuakeMLParser = QuakeMLParser()
