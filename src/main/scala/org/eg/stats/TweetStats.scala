package org.eg.stats

import java.util.concurrent.atomic.AtomicReference
import java.util.function.BinaryOperator

import cats.Applicative
import cats.effect.Sync
import cats.kernel.Monoid
import fs2.Pipe
import io.circe.Json

import scala.concurrent.ExecutionContext

final case class TweetStats(totalCount: Int) {
  def add(delta: Delta1): TweetStats = {
    TweetStats(
      totalCount = totalCount + 1
    )
  }
}

object TweetStats {

  val empty= TweetStats(totalCount = 0)

  private [stats] val currentStats = new AtomicReference[TweetStats](empty)

  /** @return updated tweet-stats */
  private [stats] def accumulate(delta: Delta1): TweetStats =
    currentStats.accumulateAndGet(empty,
      (current: TweetStats, _) => current.add(delta)
    )

  import io.circe._, io.circe.generic.semiauto._
  import org.http4s.circe._
  import org.http4s.{EntityDecoder, EntityEncoder}

  implicit val tweetStatsDecoder: Decoder[TweetStats] = deriveDecoder[TweetStats]
  implicit def tweetStatsEntityDecoder[F[_]: Sync]: EntityDecoder[F, TweetStats] =
    jsonOf

  implicit val tweetStatsEncoder: Encoder[TweetStats] = deriveEncoder[TweetStats]
  implicit def tweetStatsEntityEncoder[F[_]: Applicative]: EntityEncoder[F, TweetStats] =
    jsonEncoderOf
}
