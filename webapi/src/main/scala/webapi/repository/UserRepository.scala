package webapi.repository

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.Key
import farango.data.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import webapi.model.User
import zio.Task
import zio.ZIO

import java.time.ZonedDateTime

trait UserRepository:

  def add(user: User): Task[User]

  def get(email: String): Task[Option[User]]

  def update(email: String, update: User.Update): Task[Option[User]]

  def remove(email: String): Task[Option[User]]

object UserRepository:

  def apply(collection: DocumentCollection): UserRepository =
    wire[Impl]

  private[repository] case class Document(
      _key: Key,
      name: String,
      createdAt: ZonedDateTime
  )

  private[repository] case class UpdateDocument(
      name: String
  )

  private[repository] given Conversion[User, Document] = user =>
    user
      .into[Document]
      .transform(
        Field.const(_._key, user.email: Key)
      )

  private[repository] given Conversion[Document, User] = document =>
    document
      .into[User]
      .transform(
        Field.const(_.email, document._key: String)
      )

  private[repository] given Conversion[User.Update, UpdateDocument] = update =>
    update
      .into[UpdateDocument]
      .transform()

  private class Impl(collection: DocumentCollection) extends UserRepository:

    override def add(user: User): Task[User] =
      collection
        .insert[Document](user)
        .tap(_ => ZIO.logDebug("A new user was added."))
        .tapErrorCause(ZIO.logErrorCause("It was impossible to add an user!", _))

    override def get(email: String): Task[Option[User]] =
      collection.get[Document](Key.safe(email))

    override def update(email: String, update: User.Update): Task[Option[User]] =
      collection
        .update[UpdateDocument, Document](Key.safe(email), update)
        .tap(_ => ZIO.logDebug(s"User $email has been updated."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to updated user $email!", _))

    override def remove(email: String): Task[Option[User]] =
      collection
        .remove[Document](Key.safe(email))
        .tap(_ => ZIO.logDebug(s"User $email has been removed."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to remove user $email!", _))
