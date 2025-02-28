package tremors.util

extension [A](o: Option[A])
  def orEmptyString(using A =:= String): String =
    o match
      case Some(value) => value
      case None        => ""
