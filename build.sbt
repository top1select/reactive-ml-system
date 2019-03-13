name := "reactive-ml-system"

version := "0.1"

scalaVersion := "2.12.7"

lazy val akkaV = "2.5.6"
lazy val http4sVersion = "0.18.21"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.4.0",
  "org.apache.spark" %% "spark-mllib" % "2.4.0",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.0",
  
  "org.scalaz" %% "scalaz-core" % "7.3.0-M27",
  "org.scalaz" %% "scalaz-concurrent" % "7.3.0-M27",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-stream-kafka" % "1.0",
  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.10",
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "de.heikoseeberger" %% "akka-http-jackson" % "1.25.2",
//  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "org.apache.kafka" %% "kafka" % "2.1.1",
  "org.apache.kafka" % "kafka-clients" % "2.1.1",
  "org.apache.kafka" % "kafka-streams" % "2.1.1",
  "org.apache.kafka" %% "kafka-streams-scala" % "2.1.1",


"org.jpmml" % "pmml-model" % "1.4.8",
  "org.jpmml" % "pmml-evaluator" % "1.4.7",
  "org.jpmml" % "pmml-evaluator-extension" % "1.4.7",
  "org.tensorflow" % "tensorflow" % "1.13.1",
  
  "org.apache.curator" % "curator-test" % "4.1.0",
  
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-argonaut" % http4sVersion,
  
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.8",
  "com.github.nscala-time" %% "nscala-time" % "2.22.0",
  "org.typelevel" %% "cats-effect" % "1.2.0",
  "com.typesafe.play" %% "play-json" % "2.7.1",

  "beyondthelines" %% "pbdirect" % "0.1.0",

  "com.iheart" %% "ficus" % "1.4.4")

resolvers += Resolver.bintrayRepo("beyondthelines", "maven")

scalacOptions ++= Seq("-Ypartial-unification")


mainClass in Compile := Some("com.reactivemachinelearning.respond.ModelSupervisor")
