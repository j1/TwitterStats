package org.eg.stats

import java.util.concurrent.Executors

import cats.effect._
import fs2.{Pipe, Stream}
import io.circe.Decoder.Result
import io.circe.Json
import jawnfs2._
import org.http4s._
import org.http4s.client.blaze._
import org.http4s.client.oauth1
import org.typelevel.jawn.RawFacade

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * based on http4s streaming client docs
  * @see http://http4s.org/VERSION/streaming/
  **/
class TweetStream[F[_]](implicit F: ConcurrentEffect[F], cs: ContextShift[F]) {
  // jawn-fs2 needs to know what JSON AST you want
  implicit val f: RawFacade[Json] = io.circe.jawn.CirceSupportParser.facade

  //noinspection TypeParameterShadow - F[_] shadows
  def deriveStats[F[_], J <: Json](ec: ExecutionContext): Pipe[F,J,J] =
    // TODO optimize chunk size and sum up each chunk first...
    in => in.scanChunks(0L){ (i, c) =>
      ec.execute { () =>
        c.foreach { tweet: J =>
          val delta: Result[Delta1] = Delta1.fromTweet(tweet)
          delta.foreach(TweetStats.accumulate)
        }
      }
      (i + c.size, c)
    }

  /* These values are created by a Twitter developer web app.
   * OAuth signing is an effect due to generating a nonce for each `Request`.
   */
  protected def sign(consumerKey: String, consumerSecret: String, accessToken: String, accessSecret: String)
          (req: Request[F]): F[Request[F]] = {
    val consumer = oauth1.Consumer(consumerKey, consumerSecret)
    val token    = oauth1.Token(accessToken, accessSecret)
    oauth1.signRequest(req, consumer, callback = None, verifier = None, token = Some(token))
  }

  /* Create a http client, sign the incoming `Request[F]`, stream the `Response[IO]`, and
   * `parseJsonStream` the `Response[F]`.
   * `sign` returns a `F`, so we need to `Stream.eval` it to use a for-comprehension.
   */
  protected def jsonStream(consumerKey: String, consumerSecret: String, accessToken: String, accessSecret: String)
                (req: Request[F]): Stream[F, Json] =
    for {
      client <- BlazeClientBuilder(global).stream
      sr  <- Stream.eval(sign(consumerKey, consumerSecret, accessToken, accessSecret)(req))
      res <- client.stream(sr).flatMap(_.body.chunks.parseJsonStream)
    } yield res

  /* Stream the sample statuses.
   * We map over the Circe `Json` objects to pretty-print them with `spaces2`.
   * Then we `to` them to fs2's `lines` and then to `stdout` `Sink` to print them.
   */
  protected def stream(mutationEC: ExecutionContext): Stream[F, Json] = {
    import org.eg.stats.Secrets._
    val req = Request[F](Method.GET, uri"https://stream.twitter.com/1.1/statuses/sample.json")
    val s   = jsonStream(consumerApiKey, consumerApiSecret, accessToken, accessTokenSecret)(req)
    s.through(deriveStats(mutationEC))
      // pretty print json - CAUTION
      //.map(_.spaces2).through(lines).through(utf8Encode).through(stdout(mutationEC))
  }

  /**
    * We're going to be deriving statistics by mutation,
    * which uses locks and may block.  We don't
    * want to block our main threads, so we create a separate pool.  We'll use
    * `fs2.Stream` to manage the shutdown for us.
    */
  protected def mutationEcStream: Stream[F, ExecutionContext] =
    Stream.bracket(acquire = F.delay(Executors.newFixedThreadPool(4)))(
      release = pool => F.delay(pool.shutdown())
    ).map(ExecutionContext.fromExecutorService)

  def stream: Stream[F, Json]=
    mutationEcStream.flatMap { mutationEc =>
      stream(mutationEc)
    }
}
