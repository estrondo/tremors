package tremors.graboid

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import tremors.graboid.repository.TimelineRepository
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

import java.time.Duration
import java.time.ZonedDateTime
import org.mockito.ArgumentMatchers

object TimelineManagerSpec extends Spec:

  val repositoryMockLayer = ZLayer {
    ZIO.succeed(mock(classOf[TimelineRepository]))
  }

  override def spec = suite("A TimelineManager")(
    test(
      "when TimelineRepository returns None for the last window, it should compute the next window correctly"
    ) {
      val startingInstant = ZonedDateTime.now().plusHours(10)
      val defaultDuration = Duration.ofMinutes(10)
      val expectedEnding  = startingInstant.plus(defaultDuration)

      for
        repository <- ZIO.service[TimelineRepository]
        config      = TimelineManager.Config(defaultDuration, startingInstant)
        manager     = TimelineManager(config, repository)
        _           = when(repository.last(ArgumentMatchers.eq("simple-test"))).thenReturn(ZIO.none)
        nextWindow <- manager.nextWindow("simple-test")
      yield assertTrue(
        nextWindow.beginning == startingInstant,
        nextWindow.ending == expectedEnding
      )
    }.provideLayer(repositoryMockLayer),
    test("when TimelineRepository returns the last window, it should compute the next one.") {
      val startingInstant = ZonedDateTime.now().plusHours(10)
      val defaultDuration = Duration.ofMinutes(10)
      val lastEnding      = startingInstant.plus(defaultDuration)
      val expectedEnding  = lastEnding.plus(defaultDuration)
      val lastWindow      = TimelineManager.Window("---", startingInstant, lastEnding)

      for
        repository <- ZIO.service[TimelineRepository]
        config      = TimelineManager.Config(defaultDuration, startingInstant)
        manager     = TimelineManager(config, repository)
        _           =
          when(repository.last(ArgumentMatchers.eq("simple-test"))).thenReturn(ZIO.some(lastWindow))
        nextWindow <- manager.nextWindow("simple-test")
      yield assertTrue(
        nextWindow.beginning == lastEnding,
        nextWindow.ending == expectedEnding
      )
    }.provideLayer(repositoryMockLayer)
  ).provideLayer(logger)
