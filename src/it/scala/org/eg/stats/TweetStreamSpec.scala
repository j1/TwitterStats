package org.eg.stats

import cats.effect.IO
import io.circe.Json
import io.circe.generic.auto._
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, Matchers}
import twitter4s.entities.Tweet

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
        val countedTweets = result.count(json => json.as[Tweet].isRight)
        TweetStats.currentStats.get().totalCount shouldEqual before.totalCount + countedTweets
      }
    }
  }

}
