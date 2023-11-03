package toph.event

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.Mockito
import org.mockito.Mockito.verify
import toph.TophSpec
import toph.centre.EventCentre
import toph.context.TophExecutionContext
import toph.model.TophEventFixture
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.quakeml.EventFixture
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object EventListenerSpec extends TophSpec:

  def spec = suite("An EventListener")(
    test("It should receive an event.") {
      val receivedEvent     = EventFixture.createRandom()
      val expectedTophEvent = TophEventFixture.createRandom(receivedEvent)

      for
        _           <- SweetMockitoLayer[EventCentre]
                         .whenF2(_.add(eqTo(expectedTophEvent))(using anyOf[TophExecutionContext]()))
                         .thenReturn(expectedTophEvent)
        _           <- ZIO.serviceWith[KeyGenerator] { keyGenerator =>
                         when(keyGenerator.generate(anyOf[KeyLength]())).thenReturn(expectedTophEvent.id)
                       }
        event       <- ZIO.serviceWithZIO[EventListener](_.apply(receivedEvent))
        eventCentre <- ZIO.service[EventCentre]
      yield assertTrue(
        event == receivedEvent,
        verify(eventCentre).add(eqTo(expectedTophEvent))(using anyOf[TophExecutionContext]()) == null
      )
    }
  ).provideSome(
    SweetMockitoLayer.newMockLayer[EventCentre],
    SweetMockitoLayer.newMockLayer[KeyGenerator],
    ZLayer {
      for
        eventCentre  <- ZIO.service[EventCentre]
        keyGenerator <- ZIO.service[KeyGenerator]
      yield EventListener(eventCentre, keyGenerator)
    }
  )
