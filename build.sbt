
name := "reactive-ml-system"

version := "0.1"

scalaVersion := "2.12.7"

val akkaV = "2.5.6"
lazy val http4sVersion = "0.18.21"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.4.0",
  "org.apache.spark" %% "spark-mllib" % "2.4.0",
  "org.scalaz" %% "scalaz-core" % "7.3.0-M27",
  "org.scalaz" %% "scalaz-concurrent" % "7.3.0-M27",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.github.nscala-time" %% "nscala-time" % "2.22.0",
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.10",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-argonaut" % http4sVersion,
  "org.typelevel" %% "cats-effect" % "1.2.0")


scalacOptions ++= Seq("-Ypartial-unification")


mainClass in Compile := Some("com.reactivemachinelearning.respond.ModelSupervisor")