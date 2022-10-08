package tremors.graboid

import java.time.ZonedDateTime
import tremors.graboid.quakeml.model.CreationInfo

extension (window: TimelineManager.Window)
  def contains(creationInfo: Option[CreationInfo]): Boolean = creationInfo match
    case Some(value) => ???
    case _           => ???
