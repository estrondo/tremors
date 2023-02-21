package toph.module

import com.softwaremill.macwire.wire
import com.softwaremill.tagging.@@
import com.softwaremill.tagging.given
import farango.DocumentCollection
import farango.zio.starter.FarangoStarter
import toph.repository.EventDataRepository
import toph.repository.HypocentreDataRepository
import toph.repository.MagnitudeDataRepository
import toph.repository.EventRepository
import zio.Task
import zio.ZIO

trait RepositoryModule:

  val eventRepository: EventDataRepository

  val hypocentreRepository: HypocentreDataRepository

  val magnitudeRepository: MagnitudeDataRepository

  val queriableEventRepository: EventRepository

object RepositoryModule:

  trait ForEvent

  trait ForMagnitude

  case class Coll(collection: DocumentCollection)

  extension (effect: Task[DocumentCollection])
    def coll[T]: Task[Coll @@ T] =
      effect.map(Coll(_).taggedWith[T])

  def apply(
      farangoModule: FarangoModule
  ): Task[RepositoryModule] =
    for
      collEvent                <- farangoModule.collection("event").coll[ForEvent]
      collMagnitude            <- farangoModule.collection("magnitude").coll[ForMagnitude]
      hypocentreRepository     <- farangoModule.collection("hypocentre").flatMap(HypocentreDataRepository.apply)
      queriableEventRepository <- farangoModule.collection("queriable-event").flatMap(EventRepository.apply)
    yield wire[Impl]

  private class Impl(
      collEvent: Coll @@ ForEvent,
      collMagnitude: Coll @@ ForMagnitude,
      override val hypocentreRepository: HypocentreDataRepository,
      override val queriableEventRepository: EventRepository
  ) extends RepositoryModule:

    override val eventRepository: EventDataRepository = EventDataRepository(collEvent.collection)

    override val magnitudeRepository: MagnitudeDataRepository = MagnitudeDataRepository(collMagnitude.collection)
