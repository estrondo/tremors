package graboid

import com.softwaremill.macwire.wire
import com.softwaremill.macwire.wireWith
import com.softwaremill.tagging.@@
import com.softwaremill.tagging.given
import farango.FarangoDocumentCollection
import graboid.config.GraboidConfig
import zio.Task
import zio.ZIO

trait CoreModule:

  val eventPublisherManager: EventPublisherManager

object CoreModule:

  trait ForEventRecord
  trait ForEventPublisher

  case class CollectionWrapper(collection: FarangoDocumentCollection)

  val EventPublisherCollectionName = "event_publisher"
  val EventRecordCollectionName    = "event_record"

  def apply(config: GraboidConfig, arangoModule: ArangoModule): Task[CoreModule] =
    for
      eventPublisherCollection <- arangoModule
                                    .getDocumentCollection(EventPublisherCollectionName)
                                    .map(CollectionWrapper.apply)
                                    .taggedWithF[ForEventPublisher]
      eventRecordCollection    <- arangoModule
                                    .getDocumentCollection(EventRecordCollectionName)
                                    .map(CollectionWrapper.apply)
                                    .taggedWithF[ForEventRecord]
    yield wire[CoreModuleImpl]

  private class CoreModuleImpl(
      config: GraboidConfig,
      eventPublisherCollection: CollectionWrapper @@ ForEventPublisher,
      eventRecordCollection: CollectionWrapper @@ ForEventRecord
  ) extends CoreModule:

    val eventPublisherRepository = EventPublisherRepository(eventPublisherCollection.collection)

    val eventPublisherValidator: EventPublisherManager.Validator = ZIO.succeed(_)

    override val eventPublisherManager: EventPublisherManager = wireWith(EventPublisherManager.apply)
