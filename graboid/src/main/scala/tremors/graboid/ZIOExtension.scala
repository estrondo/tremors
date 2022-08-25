package tremors.graboid

import zio.ZIO

extension [R, E, A](effect: ZIO[R, E, A])
  def >>>[R1 <: R, E1 >: E, B](otherEffect: ZIO[R1, E1, B]): ZIO[R1, E1, B] =
    effect.flatMap(_ => otherEffect)
