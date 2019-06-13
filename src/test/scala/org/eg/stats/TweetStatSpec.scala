package org.eg.stats

import cats.effect.{ContextShift, IO}

import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.scalatest.{AsyncWordSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class TweetStatSpec extends AsyncWordSpec with Matchers with IOTest {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  "Get stats" should {
    "return 200" in {
      uriReturns200()
    }
    "return stats" in {
      uriReturnsStats()
    }
  }

  private[this] val tweetStatsResp: IO[Response[IO]]= {
    val tweetStatsApi = TweetStatsApi.impl[IO]
    val httpApp = TwitterstatsRoutes.tweetStatsRoutes[IO](tweetStatsApi).orNotFound

    // With Middlewares in place
    val finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

    val getStatsReq = Request[IO](Method.GET, uri"/stats")
    finalHttpApp(getStatsReq)
  }

  private[this] def uriReturns200() =
    tweetStatsResp.map(_.status shouldEqual Status.Ok)

  private[this] def uriReturnsStats() =
    tweetStatsResp.flatMap(_.as[TweetStats].map(_.totalCount should be >= 0))
}