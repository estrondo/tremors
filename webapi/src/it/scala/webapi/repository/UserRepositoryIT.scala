package webapi.repository

import farango.DocumentCollection
import farango.zio.given
import testkit.zio.repository.RepositoryIT
import webapi.IT
import webapi.model.User
import zio.Scope
import zio.Task
import zio.ZIO
import zio.test.Spec
import zio.test.TestEnvironment

import UserRepository.Document
import webapi.fixture.UserFixture
import farango.data.Key
import zio.test.TestAspect

object UserRepositoryIT extends IT:

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("A UseRepository")(
      test("It should add a new user.") {
        RepositoryIT.testAdd(UserFixture.createRandom())
      },
      test("It should update a user.") {
        val user     = UserFixture.createRandom()
        val update   = User.Update(
          name = "Einstein"
        )
        val expected = user.copy(name = "Einstein")
        RepositoryIT.testUpdate(user, update, expected)
      },
      test("It should remove a user.") {
        RepositoryIT.testRemove(UserFixture.createRandom())
      }
    ).provideSomeLayer[Scope](
      RepositoryIT.of[UserRepository, User]
    ) @@ TestAspect.sequential

  private given RepositoryIT[UserRepository, User] with

    override def create(collection: DocumentCollection): Task[UserRepository] =
      ZIO.succeed(UserRepository(collection))

    override def get(collection: DocumentCollection, value: User): Task[Option[User]] =
      collection.get[Document](Key.safe(value.email))

    override def insert(repository: UserRepository, value: User): Task[Any] =
      repository.add(value)

    override def remove(repository: UserRepository, value: User): Task[Any] =
      repository.remove(value.email)

    override def update(repository: UserRepository, originalValue: User, updateValue: Any): Task[Any] =
      repository.update(originalValue.email, updateValue.asInstanceOf[User.Update])
