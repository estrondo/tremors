package webapi.module

import webapi.manager.AccountManager
import webapi.manager.AlertManager
import zio.Task
import zio.ZIO

trait CoreModule:

  val accountManager: AccountManager

  val alertManager: AlertManager

object CoreModule:

  def apply(repositoryModule: RepositoryModule): Task[CoreModule] = ZIO.attempt {
    Impl(repositoryModule)
  }

  private class Impl(repositoryModule: RepositoryModule) extends CoreModule:

    override val accountManager: AccountManager =
      AccountManager(repositoryModule.account)

    override val alertManager: AlertManager =
      AlertManager(repositoryModule.alert)
