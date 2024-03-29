import sbt._

object Dependencies {
  val Http4sVersion = "0.20.1"
  val CirceVersion = "0.11.1"
  val ScalaTestVersion = "3.0.5"
  val ScalaCheckVersion   = "1.13.4"
  val LogbackVersion = "1.2.3"
  val BetterFilesVersion = "3.8.0"
  val GuavaVersion = "28.0-jre"
  
  lazy val http4sDeps: Seq[ModuleID] = Seq(
    "org.http4s"      %% "http4s-blaze-server",
    "org.http4s"      %% "http4s-blaze-client",
    "org.http4s"      %% "http4s-circe"       ,
    "org.http4s"      %% "http4s-dsl"         ,
  ).map(_ % Http4sVersion)
  
  lazy val scalaTest     = "org.scalatest"          %% "scalatest"           % ScalaTestVersion
  lazy val scalaCheck    = "org.scalacheck"         %% "scalacheck"          % ScalaCheckVersion
  lazy val logback       = "ch.qos.logback"         % "logback-classic"      % LogbackVersion
  lazy val betterFiles   = "com.github.pathikrit"   %% "better-files"        % BetterFilesVersion
  lazy val guava         = "com.google.guava"       % "guava"               % GuavaVersion
  
  lazy val circeDeps: Seq[ModuleID] = Seq(
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % CirceVersion)
}
