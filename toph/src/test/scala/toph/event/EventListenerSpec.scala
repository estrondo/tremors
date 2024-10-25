package toph.event

import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.Mockito
import org.mockito.Mockito.verify
import toph.TophSpec
import toph.centre.EventCentre
import toph.context.TophExecutionContext
import toph.model.TophEventFixture
import toph.model.geometryFactory
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.quakeml.EventFixture
import tremors.quakeml.MagnitudeFixture
import tremors.quakeml.OriginFixture
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object EventListenerSpec extends TophSpec:

  def spec = suite("An EventListener")(
    test("When an event is received it should send the event to the EventCentre.") {
      val origin        = OriginFixture.createRandom()
      val magnitude     = MagnitudeFixture.createRandom().copy(originId = Some(origin.publicId))
      val receivedEvent = EventFixture
        .createRandom()
        .copy(
          origin = Seq(origin),
          magnitude = Seq(magnitude),
          preferredOriginId = Some(origin.publicId),
          preferredMagnitudeId = Some(magnitude.publicId)
        )

      val expectedTophEvent = TophEventFixture.createRandom(receivedEvent, origin, magnitude)

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
      yield EventListener(eventCentre, keyGenerator, geometryFactory)
    }
  )
