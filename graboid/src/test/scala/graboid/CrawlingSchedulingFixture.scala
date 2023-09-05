package graboid

import java.time.Duration
import scala.util.Random
import tremors.ZonedDateTimeFixture
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object CrawlingSchedulingFixture:

  def createRandom(dataCentre: DataCentre): CrawlingScheduling =
    val starting = ZonedDateTimeFixture.createRandom().minusDays(Random.nextInt(3))
    val ending   = starting.plusDays(Random.nextInt(9))
    CrawlingScheduling(
      id = KeyGenerator.generate(KeyLength.Medium),
      dataCentreId = dataCentre.id,
      starting = Some(starting),
      ending = Some(ending),
      duration = Duration.ofSeconds(1800 + Random.nextInt(1800))
    )
