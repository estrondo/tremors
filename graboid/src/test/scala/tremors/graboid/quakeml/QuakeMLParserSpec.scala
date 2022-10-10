package tremors.graboid.quakeml

import tremors.graboid.Spec
import zio.stream.ZStream
import zio.test.assertTrue

object QuakeMLParserSpec extends Spec:

  def spec = suite("A QuakeMLParser")(
    test("should process a big quakeml (100K events).") {

      val expectedCount = 100000L
      val headerBytes   = """<q:quakeml 
	                      |xmlns="http://quakeml.org/xmlns/bed/1.2"
	                      |xmlns:q="http://quakeml.org/xmlns/quakeml/1.2">
                        |<eventParameters publicID="smi:org.gfz-potsdam.de/geofon/EventParameters">""".stripMargin
        .getBytes()

      val eventBytes = """<event publicID="smi:org.gfz-potsdam.de/geofon/usp2022mcfz">
			                        |<description>
			                        |	<text>MonteA.Paulista/rg.zona rural</text>
			                        |	<type>region name</type>
			                        |</description>
			                        |<creationInfo>
			                        |	<agencyID>USP</agencyID>
			                        |	<author>scevent@seisMaster</author>
			                        |	<creationTime>2022-06-28T20:24:12.964285Z</creationTime>
			                        |</creationInfo>
			                        |<magnitude publicID="smi:org.gfz-potsdam.de/geofon/Magnitude/20220628202308.368202.23737">
			                        |	<stationCount>2</stationCount>
			                        |	<creationInfo>
			                        |		<agencyID>USP</agencyID>
			                        |		<author>jroberto</author>
			                        |		<creationTime>2022-06-28T20:23:08.368217Z</creationTime>
			                        |	</creationInfo>
			                        |	<mag>
			                        |		<value>1.975109306</value>
			                        |		<uncertainty>0.08845560571</uncertainty>
			                        |	</mag>
			                        |	<type>mR</type>
			                        |	<originID>smi:org.gfz-potsdam.de/geofon/Origin/20220628202117.725002.23704</originID>
			                        |	<methodID>smi:org.gfz-potsdam.de/geofon/mean</methodID>
			                        |	<evaluationStatus>confirmed</evaluationStatus>
			                        |</magnitude>
			                        |<origin publicID="smi:org.gfz-potsdam.de/geofon/Origin/20220628202117.725002.23704">
			                        |	<time>
			                        |		<value>2022-06-21T23:43:23.057058Z</value>
			                        |		<uncertainty>0.3512440324</uncertainty>
			                        |	</time>
			                        |	<longitude>
			                        |		<value>-48.6093483</value>
			                        |		<uncertainty>2.390130281</uncertainty>
			                        |	</longitude>
			                        |	<latitude>
			                        |		<value>-20.95301628</value>
			                        |		<uncertainty>2.317521811</uncertainty>
			                        |	</latitude>
			                        |	<depthType>operator assigned</depthType>
			                        |	<quality>
			                        |		<associatedPhaseCount>11</associatedPhaseCount>
			                        |		<usedPhaseCount>11</usedPhaseCount>
			                        |		<associatedStationCount>6</associatedStationCount>
			                        |		<usedStationCount>6</usedStationCount>
			                        |		<depthPhaseCount>0</depthPhaseCount>
			                        |		<standardError>0.1225432943</standardError>
			                        |		<azimuthalGap>112.5255885</azimuthalGap>
			                        |		<maximumDistance>2.978279114</maximumDistance>
			                        |		<minimumDistance>0.1359193921</minimumDistance>
			                        |		<medianDistance>2.53863287</medianDistance>
			                        |	</quality>
			                        |	<evaluationMode>manual</evaluationMode>
			                        |	<creationInfo>
			                        |		<agencyID>USP</agencyID>
			                        |		<author>jroberto</author>
			                        |		<creationTime>2022-06-28T20:21:17.725291Z</creationTime>
			                        |	</creationInfo>
			                        |	<depth>
			                        |		<value>0</value>
			                        |		<uncertainty>0</uncertainty>
			                        |	</depth>
			                        |	<originUncertainty>
			                        |		<minHorizontalUncertainty>4962.916374</minHorizontalUncertainty>
			                        |		<maxHorizontalUncertainty>5129.860878</maxHorizontalUncertainty>
			                        |		<azimuthMaxHorizontalUncertainty>100.5883331</azimuthMaxHorizontalUncertainty>
			                        |		<preferredDescription>horizontal uncertainty</preferredDescription>
			                        |	</originUncertainty>
			                        |	<methodID>smi:org.gfz-potsdam.de/geofon/LOCSAT</methodID>
			                        |	<earthModelID>smi:org.gfz-potsdam.de/geofon/iasp91</earthModelID>
			                        |	<evaluationStatus>confirmed</evaluationStatus>
			                        |</origin>
			                        |<preferredOriginID>smi:org.gfz-potsdam.de/geofon/Origin/20220628202117.725002.23704</preferredOriginID>
			                        |<preferredMagnitudeID>smi:org.gfz-potsdam.de/geofon/Magnitude/20220628202308.368202.23737</preferredMagnitudeID>
			                        |<type>earthquake</type>
			                        |<typeCertainty>known</typeCertainty>
		                        |</event>""".stripMargin.getBytes()

      val header     = ZStream.fromIterable(headerBytes)
      val hugeStream = ZStream
        .fromIterator(Iterator.range(0L, expectedCount))
        .flatMap(i => ZStream.fromIterable(eventBytes))

      for
        stream <- QuakeMLParser().parse(header ++ hugeStream).orDieWith(identity)
        count  <- stream.runCount.orDieWith(identity)
      yield assertTrue(
        count == expectedCount
      )
    }
  ).provideLayer(logger)
