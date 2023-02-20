package testkit.zio.repository

import farango.DocumentCollection
import farango.zio.given
import testkit.core.createRandomKey
import testkit.zio.testcontainers.ArangoDBLayer
import testkit.zio.testcontainers.FarangoLayer
import zio.RIO
import zio.Tag
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer
import zio.test.TestResult
import zio.test.assertTrue

import scala.reflect.ClassTag

type Ret[R] = ZIO[R & DocumentCollection, Throwable, TestResult]

trait RepositoryIT[R, I]:

  def get(collection: DocumentCollection, value: I): Task[Option[I]]

  def get(repository: R, value: I): Task[Option[I]]

  def create(collection: DocumentCollection): Task[R]

  def insert(repository: R, value: I): Task[Any]

  def remove(repository: R, value: I): Task[Any]

  def update(repository: R, originalValue: I, updateValue: Any): Task[Any]

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
      s"repository_it_${createRandomKey()}"
    )) >+> layer

  def insertAndReturnRepo[R, I](values: Seq[I])(using RepositoryIT[R, I], Tag[R]): RIO[R, R] =
    val repositoryIT = RepositoryIT[R, I]
    for
      repository <- ZIO.service[R]
      _          <- ZIO.foreach(values)(value => repositoryIT.insert(repository, value))
    yield repository

  def testAdd[R, I](input: => I)(using RepositoryIT[R, I], Tag[R]): Ret[R] =
    val value        = input
    val repositoryIT = RepositoryIT[R, I]

    for
      repository <- ZIO.service[R]
      _          <- repositoryIT.insert(repository, value)
      collection <- ZIO.service[DocumentCollection]
      result     <- repositoryIT.get(collection, value)
    yield assertTrue(
      result == Some(value)
    )

  def testGet[R, I](input: => I)(using RepositoryIT[R, I], Tag[R]): Ret[R] =
    val value        = input
    val repositoryIT = RepositoryIT[R, I]

    for
      repository <- ZIO.service[R]
      _          <- repositoryIT.insert(repository, value)
      result     <- repositoryIT.get(repository, value)
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
      inserted   <- repositoryIT.get(collection, value)
      _          <- repositoryIT.remove(repository, value)
      empty      <- repositoryIT.get(collection, value)
    yield assertTrue(
      inserted.contains(value),
      empty.isEmpty
    )

  def testUpdate[R, I, U](input: => I, update: => U, expected: => I)(using RepositoryIT[R, I], Tag[R]): Ret[R] =
    val inputValue    = input
    val updateValue   = update
    val expectedValue = expected
    val repositoryIT  = RepositoryIT[R, I]

    for
      repository <- ZIO.service[R]
      _          <- repositoryIT.insert(repository, inputValue)
      _          <- repositoryIT.update(repository, inputValue, updateValue)
      collection <- ZIO.service[DocumentCollection]
      result     <- repositoryIT.get(collection, inputValue)
    yield assertTrue(
      result == Some(expectedValue)
    )
