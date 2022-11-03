package tremors.graboid

import scala.util.Random
import graboid.protocol.CrawlerDescriptor

object CrawlerReportFixture:

  def createRandom(descriptor: CrawlerDescriptor) = CrawlerManager.CrawlerReport(
    name = descriptor.name,
    `type` = descriptor.`type`,
    source = descriptor.source,
    success = 11 + Random.nextInt(100),
    fail = 1 + Random.nextInt(10)
  )
