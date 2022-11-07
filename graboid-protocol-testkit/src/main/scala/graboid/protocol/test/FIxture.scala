package graboid.command

import graboid.protocol.AddCrawler
import graboid.protocol.RemoveCrawler
import graboid.protocol.UpdateCrawler
import graboid.protocol.test.CrawlerDescriptorFixture
import graboid.protocol.test.UpdateCrawlerDescriptorFixture
import testkit.createRandomKey

object AddCrawlerFixture:

  def createRandom() = AddCrawler(CrawlerDescriptorFixture.createRandom())

object RemoveCrawlerFixture:

  def createRandom() = RemoveCrawler(createRandomKey())

object UpdateCrawlerFixture:

  def createRandom(name: String = createRandomKey()) =
    UpdateCrawler(name, UpdateCrawlerDescriptorFixture.createRandom(), shouldRunNow = false)
