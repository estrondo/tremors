package farango.data

import io.github.arainko.ducktape.Transformer

given Conversion[String, Key] = Key.apply

given Conversion[Key, String] = _.value

given fromConversion[A, B](using conversion: Conversion[A, B]): Transformer[A, B] =
  conversion.apply(_)
