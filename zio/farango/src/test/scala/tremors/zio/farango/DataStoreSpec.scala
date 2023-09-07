package tremors.zio.farango

import java.time.ZonedDateTime
import java.util.UUID
import scala.util.Failure
import scala.util.Success
import zio.Schedule
import zio.ZIO
import zio.ZLayer
import zio.test.TestAspect
import zio.test.ZIOSpecDefault
import zio.test.assertTrue

object DataStoreSpec extends ZIOSpecDefault:

  override def spec = suite("DataStoreSpec")(
    makeTest("It should work with Strings.", "A example", "Other value"),
    makeTest("It should work with ZonedDataTime.", ZonedDateTime.now(), ZonedDateTime.now().minusDays(3)),
    makeTest("It should work with a custom class.", MyClass(2, "A", true), MyClass(0, "B", false))
  ).provideSome(
    FarangoTestContainer.arangoContainer,
    FarangoTestContainer.farangoDB,
    FarangoTestContainer.farangoDatabase(),
    FarangoTestContainer.farangoCollection(),
    ZLayer.fromFunction(CollectionManager(_, _, Schedule.recurs(1))),
    ZLayer.fromFunction(DataStore("test", _))
  ) @@ TestAspect.sequential

  private def makeTest[T: DataStore.ToStore: DataStore.FromStore](
      label: String,
      expectedOriginal: T,
      expectedUpdate: T
  ) =
    test(label) {
      val key = newID()
      for
        _                  <- ZIO.serviceWithZIO[DataStore](_.put(key, expectedOriginal))
        original           <- ZIO.serviceWithZIO[DataStore](_.get[T](key))
        originalPreUpdate  <- ZIO.serviceWithZIO[DataStore](_.put(key, expectedUpdate))
        updated            <- ZIO.serviceWithZIO[DataStore](_.get[T](key))
        updatedPreRemove   <- ZIO.serviceWithZIO[DataStore](_.remove[T](key))
        updatedAfterRemove <- ZIO.serviceWithZIO[DataStore](_.get[T](key))
      yield assertTrue(
        original.contains(expectedOriginal),
        originalPreUpdate.contains(expectedOriginal),
        updated.contains(expectedUpdate),
        updatedPreRemove.contains(expectedUpdate),
        updatedAfterRemove.isEmpty
      )
    }

  def newID(): String = UUID.randomUUID().toString

  private given DataStore.ToStore[MyClass] = { myClass =>
    ZIO.succeed(s"${myClass.i}\n${myClass.s}\n${myClass.b}")
  }

  private given DataStore.FromStore[MyClass] = { string =>
    ZIO.fromTry {
      string.split('\n') match
        case Array(i, s, b) => Success(MyClass(i.toInt, s, b.toBoolean))
        case _              => Failure(new RuntimeException("@@@"))
    }
  }

  case class MyClass(i: Int, s: String, b: Boolean)
