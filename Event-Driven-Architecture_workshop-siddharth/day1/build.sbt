name := "scala-eda-ecommerce"

version := "1.0"

scalaVersion := "2.13.6"

val AkkaVersion = "2.6.15"
val AkkaHttpVersion = "10.2.6"
val RabbitMQClientVersion = "5.14.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.rabbitmq" % "amqp-client" % RabbitMQClientVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.9" % Test
)

