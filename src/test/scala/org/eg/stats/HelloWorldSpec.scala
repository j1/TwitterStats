package org.eg.stats

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._

import implicits.io2Future
import org.scalatest.{AsyncWordSpec, Matchers}

class HelloWorldSpec extends AsyncWordSpec with Matchers {

  "HelloWorld" should {
    "return 200" in {
      uriReturns200()
    }
    "return hello world" in {
      uriReturnsHelloWorld()
    }
  }

  private[this] val retHelloWorld: IO[Response[IO]]= {
    val getHW = Request[IO](Method.GET, uri"/hello/world")
    val helloWorld = HelloWorld.impl[IO]
    TwitterstatsRoutes.helloWorldRoutes(helloWorld).orNotFound(getHW)
  }

  private[this] def uriReturns200() =
    retHelloWorld.map(_.status shouldEqual Status.Ok)

  private[this] def uriReturnsHelloWorld() =
    retHelloWorld.flatMap(_.as[String].map(_ shouldEqual "{\"message\":\"Hello, world\"}"))
}