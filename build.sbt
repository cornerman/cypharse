name := "cypharse"

version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies ++=
  "org.neo4j" % "neo4j-cypher-frontend-3.0" % "3.0.4" ::
  "com.github.cornerman" %% "macroni" % "0.0.1-SNAPSHOT" % "test" ::
  "org.specs2" %% "specs2-core" % "3.8.4" % "test" ::
  "org.specs2" %% "specs2-mock" % "3.8.4" % "test" ::
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
