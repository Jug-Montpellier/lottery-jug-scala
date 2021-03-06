enablePlugins(JDKPackagerPlugin)
enablePlugins(PlantUMLPlugin)

organization := "org.jug-montpellier"

name := "lottery-jug-scala"

version := "1.0"

scalaVersion := "2.12.7"

val akkaVersion  = "2.5.17"
val akkaHttpVersion = "10.1.5"
val circeVersion = "0.10.0"

javacOptions ++= Seq("-source",
                     "1.8",
                     "-target",
                     "1.8",
                     "-Xlint",
                     "-Xlog-implicits")

scalacOptions := Seq("-deprecation", "-feature", "-language:postfixOps")

libraryDependencies ++= Seq("akka-stream", "akka-slf4j").map("com.typesafe.akka" %% _ % akkaVersion)

libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaHttpVersion exclude ("org.slf4j", "slf4j-log4j12")

libraryDependencies ++= Seq(
  "circe-core",
  "circe-generic",
  "circe-parser",
  "circe-optics"
).map(d => "io.circe" %% d % circeVersion)

libraryDependencies += "de.heikoseeberger" %% "akka-http-circe" % "1.22.0"

libraryDependencies += "com.typesafe" % "config" % "1.3.3"

libraryDependencies += "org.slf4j" % "log4j-over-slf4j" % "1.7.25"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "ch.megard" %% "akka-http-cors" % "0.3.1"

libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.0.5" % "test")

scalacOptions in Test ++= Seq("-Yrangepos")

mainClass in Compile := Some("lottery.WebServer")

cancelable in Global := true
