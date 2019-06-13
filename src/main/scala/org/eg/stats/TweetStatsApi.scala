package org.eg.stats

import cats.Applicative
import cats.implicits._

trait TweetStatsApi[F[_]]{
  def stats: F[TweetStatsPresentation]
}

object TweetStatsApi {
  implicit def apply[F[_]](implicit ev: TweetStatsApi[F]): TweetStatsApi[F] = ev

  def impl[F[_]: Applicative]: TweetStatsApi[F] = new TweetStatsApi[F]{
    def stats: F[TweetStatsPresentation] =
      TweetStats.currentStats.get.present.pure[F]
  }
}