package tremors.graboid

import org.mockito.ArgumentMatchers.{eq => meq, *}
import org.mockito.Mockito.*
import tremors.graboid.repository.TimelineRepository
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

import java.time.Duration
import java.time.ZonedDateTime
import org.mockito.ArgumentMatchers
import java.time.temporal.ChronoUnit

object TimelineManagerSpec extends Spec:

  val timelineRepositoryMockLayer = ZLayer {
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
    }.provideLayer(timelineRepositoryMockLayer),
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
    }.provideLayer(timelineRepositoryMockLayer),
    test("when TimelineManager registers a new window, it should call TimelineRepository.add") {

      val beginning = ZonedDateTime.now().plusDays(29)
      val ending    = beginning.plusDays(13)

      val window = TimelineManager.Window("---id---", beginning, ending)
      for
        repository       <- ZIO.service[TimelineRepository]
        config            = TimelineManager.Config(Duration.of(10, ChronoUnit.DAYS), beginning)
        _                 = when(repository.add(meq("testable"), meq(window))).thenReturn(ZIO.succeed(window))
        manager           = TimelineManager(config, repository)
        registeredWindow <- manager.register("testable", window)
      yield assertTrue(
        registeredWindow == window,
        verify(repository).add(meq("testable"), meq(window)) == null
      )

    }.provideLayer(timelineRepositoryMockLayer)
  ).provideLayer(logger)
