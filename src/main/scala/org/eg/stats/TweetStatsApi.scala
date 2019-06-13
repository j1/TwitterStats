package org.eg.stats

import cats.Applicative
import cats.implicits._

trait TweetStatsApi[F[_]]{
  def stats: F[TweetStats]
}

object TweetStatsApi {
  implicit def apply[F[_]](implicit ev: TweetStatsApi[F]): TweetStatsApi[F] = ev

  def impl[F[_]: Applicative]: TweetStatsApi[F] = new TweetStatsApi[F]{
    def stats: F[TweetStats]=
      TweetStats.currentStats.get.pure[F]
  }
}