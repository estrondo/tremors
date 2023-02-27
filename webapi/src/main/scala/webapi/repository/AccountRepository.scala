package webapi.repository

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.Key
import farango.data.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import webapi.model.Account
import zio.Task
import zio.ZIO

import java.time.ZonedDateTime

trait AccountRepository:

  def activate(email: String): Task[Option[Account]]

  def add(account: Account): Task[Account]

  def get(email: String): Task[Option[Account]]

  def update(email: String, update: Account.Update): Task[Option[Account]]

  def remove(email: String): Task[Option[Account]]

object AccountRepository:

  def apply(collection: DocumentCollection): AccountRepository =
    wire[Impl]

  private[repository] case class Document(
      _key: Key,
      name: String,
      active: Boolean,
      secret: String,
      createdAt: ZonedDateTime
  )

  private[repository] case class UpdateDocument(
      name: String
  )

  private[repository] case class ActivateDocument(
      _key: Key,
      active: Boolean
  )

  private[repository] given Conversion[Account, Document] = account =>
    account
      .into[Document]
      .transform(
        Field.const(_._key, account.email: Key)
      )

  private[repository] given Conversion[Document, Account] = document =>
    document
      .into[Account]
      .transform(
        Field.const(_.email, document._key: String)
      )

  private[repository] given Conversion[Account.Update, UpdateDocument] = update =>
    update
      .into[UpdateDocument]
      .transform()

  private[repository] given Conversion[ActivateDocument, ActivateDocument] = identity

  private class Impl(collection: DocumentCollection) extends AccountRepository:

    override def activate(email: String): Task[Option[Account]] =
      collection
        .update[ActivateDocument, Document](Key.safe(email), ActivateDocument(email, true))

    override def add(account: Account): Task[Account] =
      collection
        .insert[Document](account)
        .tap(_ => ZIO.logDebug("A new account was added."))
        .tapErrorCause(ZIO.logErrorCause("It was impossible to add an account!", _))

    override def get(email: String): Task[Option[Account]] =
      collection.get[Document](Key.safe(email))

    override def update(email: String, update: Account.Update): Task[Option[Account]] =
      collection
        .update[UpdateDocument, Document](Key.safe(email), update)
        .tap(_ => ZIO.logDebug(s"User $email has been updated."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to updated account $email!", _))

    override def remove(email: String): Task[Option[Account]] =
      collection
        .remove[Document](Key.safe(email))
        .tap(_ => ZIO.logDebug(s"User $email has been removed."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to remove account $email!", _))
