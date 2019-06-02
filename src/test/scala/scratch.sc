
import java.util.concurrent.atomic.AtomicLong

import cats.effect._
import fs2.Stream

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
implicit val timer: Timer[IO] = IO.timer(global)

val seconds = Stream.awakeEvery[IO](1.second)
//val runningCount = seconds.mapAccumulate (0)((i, ts) => (i+1, ()))

//runningCount.take(10).compile.toVector.unsafeRunSync()

import fs2._
// import fs2._

def tk[F[_],O](n: Long): Pipe[F,O,O] =
  in => in.scanChunksOpt(n) { n =>
    if (n <= 0) None
    else Some(c => c.size match {
      case m if m < n => (n - m, c)
      case m => (0, c.take(n.toInt))
    })
  }
// tk: [F[_], O](n: Long)fs2.Pipe[F,O,O]

Stream(1,2,3,4).through(tk(2)).toList
val N = new AtomicLong(0)
def count[F[_],O]: Pipe[F,O,O] =
  in => in.scanChunks(0L) { (i, c) =>
    val j = i + c.size
    global.execute(()=>N.set(j))
    (j, c)
  }

seconds.through(count)
  .compile.drain.unsafeRunAsync(_ => ())

Thread.sleep(10 * 1000)
N