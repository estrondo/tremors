package webapi.manager

import webapi.WebAPIException
import webapi.model.Alert
import webapi.repository.AlertRepository
import zio.RIO
import zio.Task
import zio.ZIO
import webapi.model.Alert.Update
import zio.stream.ZStream

trait AlertManager:

  def add(alert: Alert): RIO[UserManager, Alert]

  def all(): Task[ZStream[Any, Throwable, Alert]]

  def enable(key: String, enabled: Boolean): Task[Option[Alert]]

  def update(key: String, update: Alert.Update): Task[Option[Alert]]

  def remove(key: String): Task[Option[Alert]]

object AlertManager:

  def apply(repository: AlertRepository): AlertManager =
    Impl(repository)

  private class Impl(repository: AlertRepository) extends AlertManager:

    override def add(alert: Alert): RIO[UserManager, Alert] =
      for
        opt    <- ZIO.serviceWithZIO[UserManager](_.get(alert.email))
        result <- if opt.isDefined then _add(alert)
                  else ZIO.fail(WebAPIException.Invalid(s"There is no user with email: ${alert.email}."))
      yield result

    override def all(): Task[ZStream[Any, Throwable, Alert]] =
      repository.all()

    override def enable(key: String, enabled: Boolean): Task[Option[Alert]] =
      repository
        .enable(key, enabled)
        .tap(_ => ZIO.logDebug(s"Alert $key has been changed enabled=$enabled."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to enable/disable alert $key!", _))

    override def remove(key: String): Task[Option[Alert]] =
      repository
        .remove(key)
        .tap(_ => ZIO.logDebug(s"Alert $key has been removed."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to remove alert $key!", _))

    override def update(key: String, update: Update): Task[Option[Alert]] =
      repository
        .update(key, update)
        .tap(_ => ZIO.logDebug(s"Alert $key has been updated."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to update alert $key!", _))

    private def _add(alert: Alert): Task[Alert] =
      repository
        .add(alert)
        .tap(_ => ZIO.logDebug(s"A new alert was added for email: ${alert.email}."))
        .tapErrorCause(ZIO.logErrorCause("I was impossible to add a new alert!", _))
