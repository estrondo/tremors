package farango.data

import farango.FAsync

extension [A, F[_]: FAsync](a: F[A]) def option: F[Option[A]] = FAsync[F].map(a)(Option.apply)
