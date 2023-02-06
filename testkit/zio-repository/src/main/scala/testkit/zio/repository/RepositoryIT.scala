package testkit.zio.repository

import farango.DocumentCollection
import testkit.core.createRandomKey
import testkit.zio.testcontainers.ArangoDBLayer
import testkit.zio.testcontainers.FarangoLayer
import zio.Tag
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer
import zio.test.TestResult
import zio.test.assertTrue
import farango.zio.given
import scala.reflect.ClassTag

type Ret[R] = ZIO[R & DocumentCollection, Throwable, TestResult]

trait RepositoryIT[R, I]:

  def get(collection: DocumentCollection, key: String): Task[Option[I]]

  def create(collection: DocumentCollection): Task[R]

  def insert(repository: R, value: I): Task[Any]

  def remove(repository: R, value: I): Task[Any]

  def getKey(value: I): String

object RepositoryIT:

  inline transparent def apply[R, I](using inline r: RepositoryIT[R, I]): RepositoryIT[R, I] = r

  def of[R: Tag, I](using RepositoryIT[R, I]): TaskLayer[R & DocumentCollection] =
    val layer = ZLayer {
      for
        collection <- ZIO.service[DocumentCollection]
        repository <- RepositoryIT[R, I].create(collection)
      yield repository
    }

    (ArangoDBLayer.layer >>> FarangoLayer.database >>> FarangoLayer.documentCollectionLayer(
      s"repository_it_${createRandomKey(8)}"
    )) >+> layer

  def testAdd[R, I](input: => I)(using RepositoryIT[R, I], Tag[R]): Ret[R]    =
    val value        = input
    val repositoryIT = RepositoryIT[R, I]

    for
      repository <- ZIO.service[R]
      _          <- repositoryIT.insert(repository, value)
      collection <- ZIO.service[DocumentCollection]
      result     <- repositoryIT.get(collection, repositoryIT.getKey(value))
    yield assertTrue(
      result == Some(value)
    )

  def testRemove[R, I](input: => I)(using RepositoryIT[R, I], Tag[R]): Ret[R] =
    val value        = input
    val repositoryIT = RepositoryIT[R, I]

    for
      repository <- ZIO.service[R]
      _          <- repositoryIT.insert(repository, value)
      collection <- ZIO.service[DocumentCollection]
      inserted   <- repositoryIT.get(collection, repositoryIT.getKey(value))
      _          <- repositoryIT.remove(repository, value)
      empty      <- repositoryIT.get(collection, repositoryIT.getKey(value))
    yield assertTrue(
      inserted == Some(value),
      empty == None
    )
