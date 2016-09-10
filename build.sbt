name := "cypharse"

version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies ++=
  "org.neo4j" % "neo4j" % "3.0.4" ::
  "org.neo4j" % "neo4j-cypher" % "3.0.4" ::
  "org.scala-lang" % "scala-compiler" % scalaVersion.value ::
  "org.scala-lang" % "scala-reflect" % scalaVersion.value ::
  "com.github.cornerman" %% "macroni" % "0.0.1" % "test" ::
  "org.specs2" %% "specs2-core" % "3.8.4" ::
  "org.specs2" %% "specs2-mock" % "3.8.4" ::
  Nil

scalacOptions ++=
  "-encoding" :: "UTF-8" ::
  "-unchecked" ::
  "-deprecation" ::
  "-explaintypes" ::
  "-feature" ::
  "-language:_" ::
  "-Xlint:_" ::
  "-Ywarn-unused" ::
  Nil
