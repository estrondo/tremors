package testkit.graboid.protocol

import graboid.protocol.AddCrawler
import graboid.protocol.RemoveCrawler
import graboid.protocol.UpdateCrawler
import testkit.core.createRandomKey
import testkit.graboid.protocol.CrawlerDescriptorFixture
import testkit.graboid.protocol.UpdateCrawlerDescriptorFixture

object AddCrawlerFixture:

  def createRandom() = AddCrawler(CrawlerDescriptorFixture.createRandom())

object RemoveCrawlerFixture:

  def createRandom() = RemoveCrawler(createRandomKey())

object UpdateCrawlerFixture:

  def createRandom(name: String = createRandomKey()) =
    UpdateCrawler(name, UpdateCrawlerDescriptorFixture.createRandom(), shouldRunNow = false)
