package org.eg.stats

import cats.effect.{ExitCode, IO}

import scala.language.implicitConversions
import scala.concurrent.Future


/**
  * Similar to IOApp, but for use with scala async tests.
  * Provides  ConcurrentEffect and ContextShift from IOApp.
  **/
trait IOTest extends cats.effect.IOApp {
  /**
    * Enables using scalatest AsyncSpec for functions of the form f: x -> IO[Y] so that
    * test code is not sprinkled with IO runs like `unsafeRunSync`
    * <p>
    * scalatest supports AsyncSpec that returns Future[Assert].
    * This io2Future implicit converts IO -> Future, so that test code can just
    * use and return IO instead of Future.
    */
  implicit def io2Future[T](io:IO[T]): Future[T] = io.unsafeToFuture()

  /** IOTest is not runnable like IOApp and IOApp.run is not used in scala test */
  override def run(args: List[String]): IO[ExitCode] = ???
}
