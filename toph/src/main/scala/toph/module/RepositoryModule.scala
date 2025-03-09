package toph.module

import com.softwaremill.macwire.wire
import com.softwaremill.tagging.*
import one.estrondo.farango.IndexDescription
import one.estrondo.farango.IndexDescription.Persistent
import toph.module.RepositoryModule.SystemTag
import toph.module.RepositoryModule.UserTag
import toph.repository.AccountRepository
import toph.repository.EventRepository
import toph.repository.ObjectStorageRepository
import toph.repository.TokenRepository
import tremors.zio.farango.FarangoModule
import zio.Task

class RepositoryModule(
    val eventRepository: EventRepository,
    val userRepository: AccountRepository,
    val tokenRepository: TokenRepository,
    val systemObjectStorageRepository: ObjectStorageRepository @@ SystemTag,
    val userObjectStorageRepository: ObjectStorageRepository @@ UserTag,
)

object RepositoryModule:

  def apply(farangoModule: FarangoModule): Task[RepositoryModule] =
    for
      eventRepository               <- farangoModule
                                         .collection(
                                           "event",
                                         )
                                         .flatMap(EventRepository.apply)
      userRepository                <- farangoModule
                                         .collection(
                                           "user",
                                           Seq(
                                             IndexDescription.Persistent(Seq("email")),
                                           ),
                                         )
                                         .flatMap(AccountRepository.apply)
      tokenRepository               <- farangoModule
                                         .collection(
                                           name = "token",
                                         )
                                         .map(TokenRepository.apply)
      systemObjectStorageRepository <- farangoModule
                                         .collection(
                                           "system-object",
                                           Seq(IndexDescription.Persistent(Seq("owner", "path"))),
                                         )
                                         .map(ObjectStorageRepository.apply)
                                         .map(_.taggedWith[SystemTag])
      userObjectStorageRepository   <- farangoModule
                                         .collection("user-object", Seq(Persistent(Seq("owner", "path"))))
                                         .map(ObjectStorageRepository.apply)
                                         .map(_.taggedWith[UserTag])
    yield wire[RepositoryModule]

  trait SystemTag

  trait UserTag
