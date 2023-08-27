package tremors.zio.http

import zio.Task
import zio.http.Response
import zio.stream.ZSink

def getContentAsString(response: Response, limit: Int): Task[String] =
  for buffer <- response.body.asStream.run(ZSink.collectAllN(limit))
  yield buffer.asString
