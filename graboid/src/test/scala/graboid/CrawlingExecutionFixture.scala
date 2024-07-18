package graboid

import graboid.crawling.CrawlingExecution
import scala.util.Random
import tremors.ZonedDateTimeFixture
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object CrawlingExecutionFixture:

  def createNew(): CrawlingExecution =
    val createdAt = ZonedDateTimeFixture.createRandom()

    val starting = ZonedDateTimeFixture.createRandom()
    val ending   = starting.plusMinutes(10)

    CrawlingExecution(
      id = KeyGenerator.generate(KeyLength.Medium),
      dataCentreId = KeyGenerator.generate(KeyLength.Medium),
      createdAt = createdAt,
      updatedAt = Some(createdAt.plusMinutes(Random.nextInt(5))),
      starting = starting,
      ending = ending,
      detected = Random.nextLong(500),
      state = CrawlingExecution.State.Completed
    )
