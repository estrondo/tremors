package webapi1x.graboid

import webapi1x.handler.CrawlerHandler
import graboid.protocol.CrawlerDescriptor
import io.github.arainko.ducktape.*
import scala.util.Try

object CrawlerDescriptorMapper:

  def from(command: CrawlerHandler.CreateCrawler): Try[CrawlerDescriptor] =
    Try {
      command
        .into[CrawlerDescriptor]
        .transform(
          Field.renamed(_.key, _.id)
        )
    }
