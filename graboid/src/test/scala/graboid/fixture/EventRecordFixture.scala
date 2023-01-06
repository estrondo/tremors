package graboid.fixture

import core.KeyGenerator
import graboid.EventRecord
import io.bullet.borer.derivation.key

object EventRecordFixture:

  def createRandom(): EventRecord = EventRecord(
    key = KeyGenerator.next32(),
    publisherKey = KeyGenerator.next32(),
    message = KeyGenerator.next32(),
    eventInstant = createZonedDateTime(),
    timeWindowKey = Some(KeyGenerator.next32())
  )

  def createRandomSeq(num: Int)(fn: (EventRecord) => EventRecord): Seq[EventRecord] =
    for _ <- 0 until num yield fn(createRandom())
