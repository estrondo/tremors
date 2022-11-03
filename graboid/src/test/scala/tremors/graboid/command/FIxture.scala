package tremors.graboid.command

import tremors.graboid.createRandomString
import graboid.protocol.AddCrawler
import graboid.protocol.RemoveCrawler
import graboid.protocol.UpdateCrawler

object AddCrawlerFixture:

  def createRandom() = AddCrawler(CrawlerDescriptorFixture.createRandom())

object RemoveCrawlerFixture:

  def createRandom() = RemoveCrawler(createRandomString())

object UpdateCrawlerFixture:

  def createRandom(name: String = createRandomString()) =
    UpdateCrawler(name, CrawlerDescriptorFixture.createRandom(), shouldRunNow = false)
