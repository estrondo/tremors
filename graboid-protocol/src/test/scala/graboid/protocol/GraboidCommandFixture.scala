package graboid.protocol

import java.time.Duration
import scala.util.Random
import tremors.ZonedDateTimeFixture
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object GraboidCommandFixture:

  def createDataCentre(): CreateDataCentre = CreateDataCentre(
    commandId = KeyGenerator.generate(KeyLength.Short),
    id = KeyGenerator.generate(KeyLength.Long),
    eventEndpoint = Some(s"http://${KeyGenerator.generate(KeyLength.Long)}/fdsn/event/1"),
    dataselectEndpoint = Some(s"http://${KeyGenerator.generate(KeyLength.Long)}/fdsn/dataselect/1")
  )

  def updateDataCentre(): UpdateDataCentre = UpdateDataCentre(
    commandId = KeyGenerator.generate(KeyLength.Short),
    KeyGenerator.generate(KeyLength.Long),
    eventEndpoint = Some(s"http://${KeyGenerator.generate(KeyLength.Long)}/fdsn/event/1"),
    dataselectEndpoint = Some(s"http://${KeyGenerator.generate(KeyLength.Long)}/fdsn/dataselect/1")
  )

  def deleteDataCentre(): DeleteDataCentre = DeleteDataCentre(
    commandId = KeyGenerator.generate(KeyLength.Short),
    id = KeyGenerator.generate(KeyLength.Long)
  )

  def runEventCrawling(): RunEventCrawling =
    val now = ZonedDateTimeFixture.createRandom()
    RunEventCrawling(
      commandId = KeyGenerator.generate(KeyLength.Medium),
      starting = now,
      ending = now.plusMinutes(Random.nextInt(10)),
      timeWindow = Duration.ofMinutes(Random.nextInt(5)),
      minMagnitude = Some(Random.nextDouble() * 3),
      maxMagnitude = Some(Random.nextDouble() * 8),
      magnitudeType = Some("magType"),
      eventType = Some("earthquake")
    )

  def runDataCentreEventCrawling(): RunDataCentreEventCrawling =
    val now = ZonedDateTimeFixture.createRandom()
    RunDataCentreEventCrawling(
      commandId = KeyGenerator.generate(KeyLength.Medium),
      dataCentre = KeyGenerator.generate(KeyLength.Short),
      starting = now,
      ending = now.plusMinutes(Random.nextInt(10)),
      timeWindow = Duration.ofMinutes(Random.nextInt(5)),
      minMagnitude = Some(Random.nextDouble() * 3),
      maxMagnitude = Some(Random.nextDouble() * 8),
      magnitudeType = Some("magType"),
      eventType = Some("earthquake")
    )
