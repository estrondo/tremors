package graboid

import java.time.ZonedDateTime
import _root_.quakeml.CreationInfo

extension (window: TimelineManager.Window)
  def contains(creationInfo: Option[CreationInfo]): Boolean =
    val TimelineManager.Window(_, beginning, ending) = window
    creationInfo match
      case Some(CreationInfo(_, _, _, _, Some(time), _)) =>
        beginning.compareTo(time) <= 0 && time.compareTo(ending) <= 0
      case _                                             =>
        false
