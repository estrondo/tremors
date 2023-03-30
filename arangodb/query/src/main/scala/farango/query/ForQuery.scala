package farango.query

import ForQuery.*
import farango.Database
import farango.Effect
import farango.EffectConversion
import farango.EffectStream
import scala.reflect.ClassTag

object ForQuery:

  sealed trait Projection:
    def render(forQuery: ForQuery): String

  sealed trait Collection:
    def render(): String

  sealed trait FilterExpression:
    def render(): String

  class Filter(expression: FilterExpression, val parameters: Map[String, Any]):
    def render(): String = expression.render()

  class PureFilterExpression(expression: String) extends FilterExpression:
    override def render(): String = expression

  class PureCollectionExpression(name: String) extends Collection:
    override def render(): String = name

  object ReturnElement extends Projection:
    override def render(forQuery: ForQuery): String = forQuery.variableName

  given Conversion[String, FilterExpression] = PureFilterExpression(_)
  given Conversion[String, Collection]       = PureCollectionExpression(_)

  class PartiallyApplied[T](database: Database, origin: ForQuery):

    def query[O, F[_]: Effect, S[_]]()(using ClassTag[T], EffectConversion[T, O, F], EffectStream[S, F]): F[S[O]] =
      val queryEffect  = Effect[F].succeed(origin.buildQueryExpression())
      val paramsEffect = Effect[F].succeed(origin.buildParameters())

      Effect[F].flatMap(queryEffect) { query =>
        Effect[F].flatMap(paramsEffect) { params =>
          database.query(query, params)
        }
      }

case class ForQuery(
    collection: Collection,
    variableName: String = "d",
    projection: Projection = ReturnElement,
    filters: Vector[Filter] = Vector.empty
):

  def apply[T](database: Database): PartiallyApplied[T] = PartiallyApplied(database, this)

  def filter(expression: FilterExpression, parameters: Map[String, Any] = Map.empty): ForQuery =
    copy(filters = filters :+ Filter(expression, parameters))

  def filter(expression: FilterExpression, parameters: (String, Any)*): ForQuery =
    filter(expression, Map.from(parameters))

  def inRangeFilter(
      value: String,
      low: (String, Option[Any]),
      high: (String, Option[Any]),
      iLow: Boolean,
      iHigh: Boolean
  ): ForQuery =
    val (lowParam, lowOpt)   = low
    val (highParam, highOpt) = high

    (lowOpt, highOpt) match
      case (Some(low), Some(high)) =>
        filter(
          s"IN_RANGE($value, @$lowParam, @$highParam, @iLow, @iHigh)",
          lowParam  -> low,
          highParam -> high,
          "iLow"    -> iLow,
          "iHigh"   -> iHigh
        )

      case (Some(low), _) =>
        filter(s"$value ${if iLow then ">=" else ">"} @$lowParam", lowParam -> low)

      case (_, Some(high)) =>
        filter(s"$value ${if iHigh then "<=" else "<"} @$highParam", highParam -> high)

      case _ => this

  private def buildQueryExpression(): String =
    val builder = StringBuilder()
    builder.append(s"FOR $variableName IN ${collection.render()}")
    for filter <- filters do builder.append(s"\n  FILTER ${filter.render()}")

    builder.append(s"\n  RETURN ${projection.render(this)}")
    builder.result()

  private def buildParameters(): Map[String, Any] =
    filters.foldLeft(Map.empty[String, Any]) { (map, filter) =>
      map ++ filter.parameters
    }
