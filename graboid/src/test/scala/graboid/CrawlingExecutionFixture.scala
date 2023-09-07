package graboid

import java.time.temporal.ChronoUnit
import scala.util.Random
import tremors.ZonedDateTimeFixture
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object CrawlingExecutionFixture:

  def createNew(): CrawlingExecution =
    val createdAt = ZonedDateTimeFixture.createRandom()
    CrawlingExecution(
      id = KeyGenerator.generate(KeyLength.Medium),
      schedulingId = KeyGenerator.generate(KeyLength.Medium),
      createdAt = createdAt,
      updatedAt = Some(createdAt.plus(Random.nextInt(5), ChronoUnit.MINUTES)),
      succeed = Random.nextLong(500),
      failed = Random.nextLong(50),
      state = CrawlingExecution.State.Completed
    )
