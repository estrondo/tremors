package webapi.repository

import farango.DocumentCollection
import farango.data.Key
import farango.zio.given
import testkit.zio.repository.RepositoryIT
import webapi.IT
import webapi.fixture.AlertFixture
import webapi.geom.GeometryFactory
import webapi.model.Alert
import webapi.model.Location
import webapi.model.MagnitudeFilter
import zio.Scope
import zio.Task
import zio.ZIO
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.assertTrue

import AlertRepository.Document
import AlertRepository.given

object AlertRepositoryIT extends IT:

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("An AlertRepository")(
      test("It should add an alert into collection (location=Seq(City, Country, Region)).") {
        val city    = Location.City("Cape Town", "South Africa")
        val country = Location.Country("Brazil")
        val region  = Location.Region("Atacama", "Chile")
        RepositoryIT.testAdd(AlertFixture.createRandom().copy(location = Seq(city, country, region)))
      },
      test("It should add an alert into collection (magnitudeFilter=(Less, Greater, Range)).") {
        val less    = MagnitudeFilter.Less(10, true)
        val greater = MagnitudeFilter.Greater(2, true)
        val range   = MagnitudeFilter.Range(2, 4, true)
        RepositoryIT.testAdd(AlertFixture.createRandom().copy(magnitudeFilter = Seq(greater, less, range)))
      },
      test("It should remove an alert from collection.") {
        RepositoryIT.testRemove(AlertFixture.createRandom())
      },
      test("It should disable an alert.") {
        val original = AlertFixture.createRandom()
        val disabled = original.copy(enabled = false)
        for
          repository <- RepositoryIT.insertAndReturnRepo(Seq(original))
          updated    <- repository.enable(original.key, false)
          result     <- ZIO.serviceWithZIO[DocumentCollection]({ collection =>
                          collection.get[Document](Key.safe(original.key))
                        })
        yield assertTrue(
          result == Some(disabled)
        )
      },
      test("It should enable an alert.") {
        val original = AlertFixture.createRandom().copy(enabled = false)
        val disabled = original.copy(enabled = true)
        for
          repository <- RepositoryIT.insertAndReturnRepo(Seq(original))
          updated    <- repository.enable(original.key, true)
          result     <- ZIO.serviceWithZIO[DocumentCollection]({ collection =>
                          collection.get[Document](Key.safe(original.key))
                        })
        yield assertTrue(
          result == Some(disabled)
        )
      },
      test("It should update an alert in collection.") {
        val less     = MagnitudeFilter.Less(10, true)
        val greater  = MagnitudeFilter.Greater(2, true)
        val range    = MagnitudeFilter.Range(2, 4, true)
        val city     = Location.City("Cape Town", "South Africa")
        val country  = Location.Country("Brazil")
        val region   = Location.Region("Atacama", "Chile")
        val original = AlertFixture.createRandom()
        val expected = original.copy(
          location = Seq(city, country, region),
          magnitudeFilter = Seq(less, greater, range),
          area = Some(GeometryFactory.createMultiPolygon()),
          areaRadius = Some(42)
        )
        val update   = Alert.Update(
          area = expected.area,
          areaRadius = expected.areaRadius,
          magnitudeFilter = expected.magnitudeFilter,
          location = expected.location
        )

        RepositoryIT.testUpdate(original, update, expected)
      },
      test("It should list all alerts.") {
        val alerts = (0 until 10).map(_ => AlertFixture.createRandom())
        for
          repository <- RepositoryIT.insertAndReturnRepo(alerts)
          stream     <- repository.all()
          result     <- stream.runCollect
        yield assertTrue(
          result.toSet == alerts.toSet
        )
      }
    ).provideSomeLayer[Scope](
      RepositoryIT.of[AlertRepository, Alert]
    ) @@ TestAspect.sequential

  private given RepositoryIT[AlertRepository, Alert] with

    override def create(collection: DocumentCollection): Task[AlertRepository] =
      AlertRepository(collection)

    override def get(collection: DocumentCollection, value: Alert): Task[Option[Alert]] =
      collection.get[Document](Key.safe(value.key))

    override def get(repository: AlertRepository, value: Alert): Task[Option[Alert]] =
      ???

    override def insert(repository: AlertRepository, value: Alert): Task[Any] =
      repository.add(value)

    override def remove(repository: AlertRepository, value: Alert): Task[Any] =
      repository.remove(value.key)

    override def update(repository: AlertRepository, originalValue: Alert, updateValue: Any): Task[Any] =
      repository.update(originalValue.key, updateValue.asInstanceOf[Alert.Update])
