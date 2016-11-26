name := "cypharse"

version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies ++=
  "org.neo4j" % "neo4j-cypher-frontend-3.0" % "3.0.7" ::
  "com.github.cornerman" %% "macroni" % "0.0.1-SNAPSHOT" % "test" ::
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
