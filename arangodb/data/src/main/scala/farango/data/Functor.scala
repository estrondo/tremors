package farango.data

import farango.FAsync

extension [A, F[_]: FAsync](a: F[A]) def map[B](fn: A => B): F[B] = FAsync[F].map(a)(fn)
