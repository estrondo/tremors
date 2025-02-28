package toph.repository

import java.time.Duration
import one.estrondo.farango.Collection
import one.estrondo.farango.sync.SyncDatabase
import toph.TophSpec
import tremors.zio.farango.CollectionManager
import zio.Schedule
import zio.ZIO
import zio.ZLayer

trait TophRepositorySpec extends TophSpec:

  protected val collectionManagerLayer: ZLayer[SyncDatabase & Collection, Nothing, CollectionManager] = ZLayer {
    for
      collection <- ZIO.service[Collection]
      database   <- ZIO.service[SyncDatabase]
    yield CollectionManager(collection, database, Schedule.spaced(Duration.ofSeconds(3)))
  }
