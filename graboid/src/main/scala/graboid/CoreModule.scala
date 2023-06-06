package graboid

import com.softwaremill.macwire.wire
import com.softwaremill.macwire.wireWith
import graboid.config.GraboidConfig
import zio.Task
import zio.ZIO

trait CoreModule:

  val publisherManager: PublisherManager

  val eventManager: EventManager

object CoreModule:

  def apply(
      config: GraboidConfig,
      repositoryModule: RepositoryModule,
      kafkaModule: KafkaModule,
      httpModule: HttpModule
  ): Task[CoreModule] =
    ZIO.attempt(wire[Impl])

  private class Impl(
      config: GraboidConfig,
      repositoryModule: RepositoryModule,
      kafkaModule: KafkaModule,
      httpModule: HttpModule
  ) extends CoreModule:

    val publisherValidator: PublisherManager.Validator = ZIO.succeed(_)

    private def publisherRepository = repositoryModule.publisherRepository

    override val publisherManager: PublisherManager = wireWith(PublisherManager.apply)

    override val eventManager: EventManager = EventManager(kafkaModule.producerLayer)
