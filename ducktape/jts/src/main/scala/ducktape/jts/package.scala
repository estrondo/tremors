package ducktape.jts

import io.github.arainko.ducktape.Transformer
import org.locationtech.jts.geom.Point

given Transformer[Point, Seq[Double]] = point => Seq(point.getX(), point.getY())
