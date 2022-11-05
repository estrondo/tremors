package webapi1x

import zio.test.ZIOSpecDefault

abstract class Spec extends ZIOSpecDefault:

  inline def assertTrue(inline exprs: => Boolean*) = zio.test.assertTrue(exprs: _*)
