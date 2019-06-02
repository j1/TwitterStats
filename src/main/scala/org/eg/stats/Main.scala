package org.eg.stats

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    TwitterstatsServer.stream[IO].compile.drain.as(ExitCode.Success)
}