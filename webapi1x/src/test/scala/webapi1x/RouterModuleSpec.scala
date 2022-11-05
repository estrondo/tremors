package webapi1x

import org.mockito.Mockito
import org.mockito.{ArgumentMatchers => Args}
import webapi1x.handler.GraboidHandler
import zhttp.http.Method.*
import zhttp.http.RHttpApp
import zhttp.http.*
import zio.Scope
import zio.Task
import zio.UIO
import zio.ULayer
import zio.URIO
import zio.URLayer
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.TestResult

object RouterModuleSpec extends Spec:

  type TestApp = RHttpApp[Any]

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("RouterModule")(
      suite("Checking crawlers endpoints.")(
        test("POST /crawlers") {
          checkGraboidHandler(POST -> !! / "crawlers", (h, req) => h.createCrawler(Args.eq(req)))
        },
        test("GET /crawlers") {
          checkGraboidHandler(GET -> !! / "crawlers", (h, req) => h.getInfoFromAll(Args.eq(req)))
        },
        test("GET /crawlers/abc") {
          checkGraboidHandler(
            GET -> !! / "crawlers" / "abc",
            (h, req) => h.getInfo(Args.eq("abc"), Args.eq(req))
          )
        },
        test("PUT /crawlers/bcd") {
          checkGraboidHandler(
            PUT -> !! / "crawlers" / "bcd",
            (h, req) => h.update(Args.eq("bcd"), Args.eq(req))
          )
        },
        test("DELETE /crawlers/cde") {
          checkGraboidHandler(
            DELETE -> !! / "crawlers" / "cde",
            (h, req) => h.delete(Args.eq("cde"), Args.eq(req))
          )
        }
      ).provideLayer(crawlerHandlerMockLayer)
    )

  extension (method: Method)
    private def ->(path: Path): Request = Request(
      method = method,
      url = URL(path)
    )

  private def checkGraboidHandler(
      request: Request,
      fn: (GraboidHandler, Request) => Task[Response]
  ): URIO[GraboidHandler & TestApp, TestResult] =
    for
      crawlerHandler <- ZIO.service[GraboidHandler]
      response        = Response()
      _               = Mockito
                          .when(fn(crawlerHandler, request))
                          .thenReturn(ZIO.succeed(response))
      result         <- checkRequest(request, response)
    yield result

  private def checkRequest(
      request: Request,
      expectedResponse: Response
  ): URIO[TestApp, TestResult] =
    for
      testApp  <- ZIO.service[TestApp]
      response <- testApp(request).orDieWith(_.get)
    yield assertTrue(response eq expectedResponse)

  private def crawlerHandlerLayer: ULayer[GraboidHandler] = ZLayer.succeed {
    Mockito.mock(classOf[GraboidHandler])
  }

  private def routerModuleLayer: ZLayer[GraboidHandler, Throwable, RouterModule] =
    ZLayer {
      for
        crawlerHandler <- ZIO.service[GraboidHandler]
        crawlerModule   = Mockito.mock(classOf[GraboidModule])
        _               = Mockito.when(crawlerModule.crawlerHandler).thenReturn(crawlerHandler)
        module         <- RouterModule(crawlerModule)
      yield module
    }

  private def httpLayer: ZLayer[RouterModule, Throwable, TestApp] =
    ZLayer {
      for
        module  <- ZIO.service[RouterModule]
        testApp <- module.createApp()
      yield testApp
    }

  private val crawlerHandlerMockLayer = crawlerHandlerLayer >+> routerModuleLayer >+> httpLayer
