package graboid

import com.softwaremill.macwire.wire
import com.softwaremill.macwire.wireWith
import com.softwaremill.tagging.@@
import com.softwaremill.tagging.given
import farango.DocumentCollection
import graboid.config.GraboidConfig
import zio.Task
import zio.ZIO

trait CoreModule:

  val publisherManager: PublisherManager

object CoreModule:

  trait ForPublisher

  case class CollectionWrapper(collection: DocumentCollection)

  val PublisherCollectionName = "publisher"

  def apply(config: GraboidConfig, arangoModule: ArangoModule): Task[CoreModule] =
    for publisherCollection <- arangoModule
                                      .getDocumentCollection(PublisherCollectionName)
                                      .map(CollectionWrapper.apply)
                                      .taggedWithF[ForPublisher]
    yield wire[CoreModuleImpl]

  private class CoreModuleImpl(
      config: GraboidConfig,
      publisherCollection: CollectionWrapper @@ ForPublisher
  ) extends CoreModule:

    val publisherRepository = PublisherRepository(publisherCollection.collection)

    val publisherValidator: PublisherManager.Validator = ZIO.succeed(_)

    override val publisherManager: PublisherManager = wireWith(PublisherManager.apply)
