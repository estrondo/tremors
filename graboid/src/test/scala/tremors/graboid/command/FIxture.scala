package tremors.graboid.command

import tremors.graboid.createRandomString

object AddCrawlerFixture:

  def createRandom() = AddCrawler(CrawlerDescriptorFixture.createRandom())

object RemoveCrawlerFixture:

  def createRandom() = RemoveCrawler(createRandomString())

object UpdateCrawlerFixture:

  def createRandom(name: String = createRandomString()) =
    UpdateCrawler(name, CrawlerDescriptorFixture.createRandom(), shouldRunNow = false)
