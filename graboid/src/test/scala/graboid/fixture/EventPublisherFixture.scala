package graboid.fixture

import core.KeyGenerator
import graboid.Crawler
import graboid.EventPublisher
import zio.config.derivation.name

import java.net.URL
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import scala.util.Random


object EventPublisherFixture:

  def createRandom(): EventPublisher = EventPublisher(
    key = KeyGenerator.next4(),
    name = KeyGenerator.next8(),
    url = URL(s"http://${KeyGenerator.next4()}/"),
    beginning = createZonedDateTime(),
    ending = Some(createZonedDateTime().plus(10 + Random.nextInt(10), ChronoUnit.DAYS)),
    `type` = Crawler.Type.FDSN
  )
