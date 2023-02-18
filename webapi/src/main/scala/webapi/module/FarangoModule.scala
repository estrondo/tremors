package webapi.module

import com.softwaremill.macwire.wire
import com.softwaremill.tagging.@@
import com.softwaremill.tagging.Tagger
import farango.Database
import farango.DocumentCollection
import farango.zio.given
import farango.zio.starter.ArangoConfig
import farango.zio.starter.FarangoStarter
import zio.Task

trait FarangoModule:

  def collection[W, T](name: String)(using DocumentCollection => W): Task[W @@ T]

object FarangoModule:

  def apply(config: ArangoConfig): Task[FarangoModule] =
    for database <- FarangoStarter(config)
    yield wire[Impl]

  private class Impl(database: Database) extends FarangoModule:

    override def collection[W, T](name: String)(using fn: DocumentCollection => W): Task[W @@ T] =
      database
        .documentCollection(name)
        .map(fn(_).taggedWith[T])
