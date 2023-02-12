package toph.module

import com.softwaremill.macwire.wire
import com.softwaremill.tagging.@@
import com.softwaremill.tagging.given
import farango.DocumentCollection
import farango.zio.starter.FarangoStarter
import toph.repository.EpicentreRepository
import toph.repository.EventRepository
import toph.repository.HypocentreRepository
import toph.repository.MagnitudeRepository
import zio.Task
import zio.ZIO

trait RepositoryModule:

  val eventRepository: EventRepository

  val epicentreRepository: EpicentreRepository

  val hypocentreRepository: HypocentreRepository

  val magnitudeRepository: MagnitudeRepository

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
      collEvent            <- farangoModule.collection("event").coll[ForEvent]
      collMagnitude        <- farangoModule.collection("magnitude").coll[ForMagnitude]
      epicentreRepository  <- farangoModule.collection("epicentre").flatMap(EpicentreRepository.apply)
      hypocentreRepository <- farangoModule.collection("hypocentre").flatMap(HypocentreRepository.apply)
    yield wire[Impl]

  private class Impl(
      collEvent: Coll @@ ForEvent,
      collMagnitude: Coll @@ ForMagnitude,
      override val epicentreRepository: EpicentreRepository,
      override val hypocentreRepository: HypocentreRepository
  ) extends RepositoryModule:

    override val eventRepository: EventRepository = EventRepository(collEvent.collection)

    override val magnitudeRepository: MagnitudeRepository = MagnitudeRepository(collMagnitude.collection)
