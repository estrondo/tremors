package tremors.zio.kafka

import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import zio.stream.ZStream

sealed abstract class KConPro[A, B](val topic: String):

  def mapperFunction: (String, A) => ZStream[Any, Throwable, (String, String, B)]

object KConPro:

  class AutoGeneratedKey[A, B](
      subscriptionTopic: String,
      productTopic: String,
      keyLength: KeyLength,
      mapper: (String, A) => ZStream[Any, Throwable, B],
      keyGenerator: KeyGenerator = KeyGenerator,
  ) extends KConPro[A, B](subscriptionTopic):

    override def mapperFunction: (String, A) => ZStream[Any, Throwable, (String, String, B)] =
      (key, value) => {
        for mapped <- mapper(key, value) yield (productTopic, keyGenerator.generate(keyLength), mapped)
      }
