package webapi1x.graboid

import webapi1x.handler.GraboidHandler
import graboid.protocol.CrawlerDescriptor
import io.github.arainko.ducktape.*
import scala.util.Try

object CrawlerDescriptorMapper:

  def from(command: GraboidHandler.CreateCrawlerRequest): Try[CrawlerDescriptor] =
    Try {
      command
        .into[CrawlerDescriptor]
        .transform(
          Field.renamed(_.key, _.id)
        )
    }
