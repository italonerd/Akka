name := "akka_essentials_scala"

version := "0.1"

scalaVersion := "2.13.6"
val akkVersion = "2.6.14"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkVersion,
  "org.scalatest" %% "scalatest" % "3.2.7"
)