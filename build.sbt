import Dependencies._

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    organization := "org.eg", // e.g. exempli gratia
    name := "TwitterStats",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    scalacOptions ++= Seq("-Ypartial-unification"),
    
    Defaults.itSettings,
    libraryDependencies ++= http4sDeps ++ circeDeps ++ Seq(
      betterFiles,
      scalaTest % "it,test", scalaCheck % "it,test",
      logback
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector"     % "0.9.6"),
    addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.2.4")
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings",
)
