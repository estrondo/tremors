package tremors

import scala.util.Random

extension [A](indexed: IndexedSeq[A]) def random: A = indexed(Random.nextInt(indexed.length))
