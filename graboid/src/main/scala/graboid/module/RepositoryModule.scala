package graboid.module

import com.arangodb.model.GeoIndexOptions
import com.arangodb.model.PersistentIndexOptions
import com.softwaremill.macwire.Module
import com.softwaremill.macwire.wire
import graboid.repository.EventRepository
import graboid.repository.HypocentreRepository
import one.estrondo.farango.IndexDescription
import tremors.zio.farango.FarangoModule
import zio.Task

@Module
trait RepositoryModule:

  def hypocentreRepository: HypocentreRepository

  def eventRepository: EventRepository

object RepositoryModule:

  def apply(farangoModule: FarangoModule): Task[RepositoryModule] =
    for
      hypocentreRepository <- farangoModule
                                .collection(
                                  "hypocentre",
                                  Seq(
                                    IndexDescription.Geo(Seq("centre"), GeoIndexOptions().geoJson(true))
                                  )
                                )
                                .map(HypocentreRepository.apply)
      eventRepository      <- farangoModule
                                .collection(
                                  "event",
                                  Seq(
                                    IndexDescription.Persistent(Seq("id"), PersistentIndexOptions().unique(true))
                                  )
                                )
                                .map(EventRepository.apply)
    yield wire[Impl]

  private class Impl(
      val hypocentreRepository: HypocentreRepository,
      val eventRepository: EventRepository
  ) extends RepositoryModule
