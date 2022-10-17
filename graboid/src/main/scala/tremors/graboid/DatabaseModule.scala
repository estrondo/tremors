package tremors.graboid

import tremors.graboid.repository.TimelineRepository

import zio.TaskLayer

trait DatabaseModule:

  val timelineRepository: TaskLayer[TimelineRepository]
