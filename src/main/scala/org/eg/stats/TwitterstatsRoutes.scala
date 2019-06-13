package org.eg.stats

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object TwitterstatsRoutes {

  def tweetStatsRoutes[F[_]: Sync](api: TweetStatsApi[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "stats" =>
        for {
          stats <- api.stats
          resp <- Ok(stats)
        } yield resp
    }
  }
}