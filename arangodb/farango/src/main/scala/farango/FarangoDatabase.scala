package farango

import com.arangodb.DbName
import com.arangodb.async.ArangoDBAsync
import com.arangodb.async.ArangoDatabaseAsync
import com.arangodb.mapping.ArangoJack
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.jdk.CollectionConverters.MapHasAsJava
import scala.reflect.ClassTag

trait FarangoDatabase:

  def query[T: ClassTag, F[_]: FApplicative, S[_]: FApplicativeStream](
      query: String,
      args: Map[String, Any] = Map.empty
  ): F[S[T]]

  def documentCollection[F[_] : FApplicative](name: String): F[FarangoDocumentCollection]

  private[farango] def underlying: ArangoDatabaseAsync

object FarangoDatabase:

  case class Config(
      name: String,
      user: String,
      password: String,
      hosts: Seq[(String, Int)]
  )

  def apply(config: Config): FarangoDatabase =

    val arangoJack = ArangoJack()
    arangoJack.configure { mapper =>
      mapper.registerModule(DefaultScalaModule)
    }

    val builder = ArangoDBAsync
      .Builder()
      .user(config.user)
      .password(config.password)
      .serializer(arangoJack)

    for (hostname, port) <- config.hosts
    do builder.host(hostname, port)

    FarangoDatabaseImpl(builder.build().db(DbName.of(config.name)))

private[farango] class FarangoDatabaseImpl(database: ArangoDatabaseAsync) extends FarangoDatabase:

  override def documentCollection[F[_] : FApplicative](name: String): F[FarangoDocumentCollection] =
    FarangoDocumentCollection(name, this)

  def query[T: ClassTag, F[_]: FApplicative, S[_]: FApplicativeStream](
      query: String,
      args: Map[String, Any] = Map.empty
  ): F[S[T]] =
    val expectedClass: Class[T] = summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]

    FApplicative[F].mapFromCompletionStage(
      database.query(query, args.asJava, expectedClass)
    ) { cursor =>
      FApplicativeStream[S].mapFromJavaStream(cursor.streamRemaining())(identity)
    }

  override private[farango] def underlying: ArangoDatabaseAsync = database
