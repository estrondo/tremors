package webapi.module

import com.softwaremill.macwire.wire
import webapi.repository.AccountRepository
import webapi.repository.AlertRepository
import zio.Task

trait RepositoryModule:

  val account: AccountRepository
  val alert: AlertRepository

object RepositoryModule:

  def apply(farangoModule: FarangoModule): Task[RepositoryModule] =
    for
      account <- farangoModule
                   .collection("account")
                   .map(AccountRepository.apply)
      alert   <- farangoModule
                   .collection("alert")
                   .map(AlertRepository.apply)
                   .flatten
    yield wire[Impl]

  private class Impl(
      override val account: AccountRepository,
      override val alert: AlertRepository
  ) extends RepositoryModule
