package webapi.repository

import farango.DocumentCollection
import farango.data.Key
import farango.zio.given
import testkit.zio.repository.RepositoryIT
import webapi.IT
import webapi.fixture.AccountFixture
import webapi.model.Account
import zio.Scope
import zio.Task
import zio.ZIO
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.assertTrue

import AccountRepository.Document

object AccountRepositoryIT extends IT:

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("A UseRepository")(
      test("It should add a new user.") {
        RepositoryIT.testAdd(AccountFixture.createRandom())
      },
      test("It should get an account by email.") {
        RepositoryIT.testGet(AccountFixture.createRandom())
      },
      test("It should update an account.") {
        val user     = AccountFixture.createRandom()
        val update   = Account.Update(
          name = "Einstein"
        )
        val expected = user.copy(name = "Einstein")
        RepositoryIT.testUpdate(user, update, expected)
      },
      test("It should remove an account.") {
        RepositoryIT.testRemove(AccountFixture.createRandom())
      },
      test("It should activate an account.") {
        val account = AccountFixture
          .createRandom()
          .copy(active = false)

        for
          repository <- RepositoryIT.insertAndReturnRepo(Seq(account))
          _          <- repository.activate(account.email)
          result     <- repository.get(account.email).some
        yield assertTrue(
          result == account.copy(active = true)
        )
      }
    ).provideSomeLayer[Scope](
      RepositoryIT.of[AccountRepository, Account]
    ) @@ TestAspect.sequential

  private given RepositoryIT[AccountRepository, Account] with

    override def create(collection: DocumentCollection): Task[AccountRepository] =
      ZIO.succeed(AccountRepository(collection))

    override def get(collection: DocumentCollection, value: Account): Task[Option[Account]] =
      collection.get[Document](Key.safe(value.email))

    override def get(repository: AccountRepository, value: Account): Task[Option[Account]] =
      repository.get(value.email)

    override def insert(repository: AccountRepository, value: Account): Task[Any] =
      repository.add(value)

    override def remove(repository: AccountRepository, value: Account): Task[Any] =
      repository.remove(value.email)

    override def update(repository: AccountRepository, originalValue: Account, updateValue: Any): Task[Any] =
      repository.update(originalValue.email, updateValue.asInstanceOf[Account.Update])
