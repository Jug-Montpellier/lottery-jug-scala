enablePlugins(JDKPackagerPlugin)
enablePlugins(PlantUMLPlugin)

organization := "org.jug-montpellier"

name := "lottery-jug-scala"

version := "1.0"

scalaVersion := "2.13.1"

//javacOptions ++= Seq("-source", 13, "-target", 13)
scalacOptions := Seq("-target:jvm-13")

scalafmtOnCompile := true

val akkaVersion  = "2.6.1"
val akkaHttpVersion = "10.1.11"
val circeVersion = "0.12.3"

javacOptions ++= Seq(
                     "-target",
                     "12",
                     "-Xlint",
                     "-Xlog-implicits")

scalacOptions := Seq("-deprecation", "-feature", "-language:postfixOps")

libraryDependencies ++= Seq("akka-stream", "akka-actor-typed", "akka-slf4j").map("com.typesafe.akka" %% _ % akkaVersion)

libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaHttpVersion exclude ("org.slf4j", "slf4j-log4j12")

libraryDependencies ++= Seq(
  "circe-core",
  "circe-generic",
//  "circe-generic-extras",
  "circe-parser"
).map(d => "io.circe" %% d % circeVersion)

libraryDependencies += "de.heikoseeberger" %% "akka-http-circe" % "1.29.1"

libraryDependencies += "com.typesafe" % "config" % "1.4.0"

libraryDependencies += "org.slf4j" % "log4j-over-slf4j" % "1.7.29"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "ch.megard" %% "akka-http-cors" % "0.4.2"

libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.1.0" % "test")

scalacOptions in Test ++= Seq("-Yrangepos")

mainClass in Compile := Some("lottery.WebServer")

cancelable in Global := true

//runMain / fork := true
