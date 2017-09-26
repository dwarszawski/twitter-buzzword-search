
name := "github-twitter-search"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % "10.0.4",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.8"
)

lazy val searchApp = (project in file("github-twitter-search"))
  .settings(assemblyJarName in assembly := "buzzwords-search.jar")
  .settings(mainClass in assembly := Some("pl.dwarszawski.search.SearchApp"))