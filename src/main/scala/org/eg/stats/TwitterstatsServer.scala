package org.eg.stats

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.implicits._
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import fs2.Stream

import scala.concurrent.ExecutionContext.global

object TwitterstatsServer {

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {

    val tweetStream = new TweetStream[F].stream

    val tweetStatsApi = TweetStatsApi.impl[F]
    // Combine Service Routes into an HttpApp.
    val httpApp = TwitterstatsRoutes.tweetStatsRoutes[F](tweetStatsApi).orNotFound

    // With Middlewares in place
    val finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

    val server = BlazeServerBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(finalHttpApp)
      .serve

    server
      .mergeHaltBoth(tweetStream.drain)
      .drain
  }

}