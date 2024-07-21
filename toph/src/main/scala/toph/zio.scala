package toph

import zio.ZIO

extension [R, E, A](zio: ZIO[R, E, A])
  def extractSomeError[E1](using ev: E <:< (Option[E1] | E1)): ZIO[R, E1, Option[A]] =
    zio.foldZIO(
      {
        ev(_) match
          case None        => ZIO.none
          case Some(e: E1) => ZIO.fail(e)
          case e: E1       => ZIO.fail(e)
      },
      a => ZIO.succeed(Some(a)),
    )
