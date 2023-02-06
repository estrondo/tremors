package toph.module

import com.softwaremill.macwire.wire
import zio.{ZIO, Task}
import toph.repository.EventRepository
import farango.zio.starter.FarangoStarter
import farango.DocumentCollection
import com.softwaremill.tagging.@@
import com.softwaremill.tagging.given

trait RepositoryModule:

  def eventRepository: EventRepository

object RepositoryModule:

  trait ForEvent

  case class Coll(collection: DocumentCollection)

  extension (effect: Task[DocumentCollection])
    def coll[T]: Task[Coll @@ T] =
      effect.map(Coll(_).taggedWith[T])

  def apply(
      farangoModule: FarangoModule
  ): Task[RepositoryModule] =
    for collEvent <- farangoModule.collection("event").coll[ForEvent]
    yield wire[Impl]

  private class Impl(
      collEvent: Coll @@ ForEvent
  ) extends RepositoryModule:

    override val eventRepository: EventRepository = EventRepository(collEvent.collection)
