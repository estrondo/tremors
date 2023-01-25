package farango.data

import farango.FAsync

extension [A, F[_]: FAsync](a: F[A]) def flatMap[B](fn: A => F[B]): F[B] = FAsync[F].flatMap(a)(fn)
