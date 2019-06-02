package org.eg.stats

import cats.effect.{ContextShift, IO}
import org.eg.stats.implicits.io2Future
import org.http4s._
import org.http4s.client.blaze._
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.scalatest.{AsyncWordSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class GetJokeSpec extends AsyncWordSpec with Matchers {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  "GetJoke" should {
    "return 200" in {
      uriReturns200()
    }
    "return a Joke" in {
      uriReturnsAJoke()
    }
  }

  private[this] val retAJoke: IO[Response[IO]]= BlazeClientBuilder[IO](global).resource.use { client =>
    //client <- BlazeClientBuilder[IO](global).stream
    val jokeAlg = Jokes.impl[IO](client)
    val httpApp = TwitterstatsRoutes.jokeRoutes[IO](jokeAlg).orNotFound

    // With Middlewares in place
    val finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

    val getJoke = Request[IO](Method.GET, uri"/joke")
    finalHttpApp(getJoke)
  }

  private[this] def uriReturns200() =
    retAJoke.map(_.status shouldEqual Status.Ok)

  private[this] def uriReturnsAJoke() =
    retAJoke.flatMap(_.as[String].map(_ should include  ("joke")))
}