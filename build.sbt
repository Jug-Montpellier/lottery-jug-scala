enablePlugins(JDKPackagerPlugin)
enablePlugins(PlantUMLPlugin)

organization := "org.jug-montpellier"

name := "lottery-jug-scala"

version := "1.0"

scalaVersion := "2.12.1"

val akkaVersion  = "2.4.17"
val circeVersion = "0.7.0"

javacOptions ++= Seq("-source",
                     "1.8",
                     "-target",
                     "1.8",
                     "-Xlint",
                     "-Xlog-implicits")

scalacOptions := Seq("-deprecation", "-feature", "-language:postfixOps")

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.4" exclude ("org.slf4j", "slf4j-log4j12")

libraryDependencies ++= Seq(
  "circe-core",
  "circe-generic",
  "circe-parser",
  "circe-optics"
).map(d => "io.circe" %% d % circeVersion)

libraryDependencies += "de.heikoseeberger" %% "akka-http-circe" % "1.12.0"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

libraryDependencies += "org.slf4j" % "log4j-over-slf4j" % "1.7.21"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"

libraryDependencies += "ch.megard" %% "akka-http-cors" % "0.1.11"

libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.0.1" % "test")

scalacOptions in Test ++= Seq("-Yrangepos")

mainClass in Compile := Some("lottery.WebServer")


