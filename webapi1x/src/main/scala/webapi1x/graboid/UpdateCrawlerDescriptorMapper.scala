package webapi1x.graboid

import webapi1x.handler.GraboidHandler
import scala.util.Try
import graboid.protocol.UpdateCrawlerDescriptor
import io.github.arainko.ducktape.*

object UpdateCrawlerDescriptorMapper:

  def from(request: GraboidHandler.UpdateCrawlerRequest): Try[UpdateCrawlerDescriptor] =
    Try {
      request.to[UpdateCrawlerDescriptor]
    }
