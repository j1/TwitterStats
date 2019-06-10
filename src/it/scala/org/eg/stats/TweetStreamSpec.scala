package org.eg.stats

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, Timer}
import io.circe.Json
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, Matchers}

import scala.concurrent.duration._
import scala.concurrent.Future

class TweetStreamSpec extends AsyncWordSpec with Matchers with IOTest with BeforeAndAfter
{
  val SampleSize = 5

  val tweetStreamSample: fs2.Stream[IO, Json] = new TweetStream[IO].stream.take(SampleSize)

  "tweet stream" should {
    "increase tweet count" in {
      val before = TweetStats.currentStats.get
      for {
        result <- tweetStreamSample.compile.toVector
      } yield {
        result.size shouldEqual SampleSize
        TweetStats.currentStats.get().totalCount shouldEqual before.totalCount + SampleSize
      }
    }
  }

}
