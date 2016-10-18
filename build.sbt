enablePlugins(JDKPackagerPlugin)

organization := "org.jug-montpellier"

name := "lottery-jug-scala"

version := "1.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.11"
val circeVersion = "0.5.2"


javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint", "-Xlog-implicits")

scalacOptions := Seq("-deprecation", "-feature", "-language:postfixOps")

libraryDependencies += "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion exclude("org.slf4j", "slf4j-log4j12")

libraryDependencies ++= Seq(
  "circe-core",
  "circe-generic",
  "circe-parser",
  "circe-optics"
).map(d => "io.circe" %% d % circeVersion)

libraryDependencies += "de.heikoseeberger" %% "akka-http-circe" % "1.10.1"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

libraryDependencies += "org.slf4j" % "log4j-over-slf4j" % "1.7.21"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"

libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.0.0" % "test")

scalacOptions in Test ++= Seq("-Yrangepos")

mainClass in Compile := Some("lottery.WebServer")

resourceGenerators in Compile <+= Def.task[Seq[File]] {
  sys.env.get("PLANTUML").map {
    path =>
      val inputs = IO.listFiles(baseDirectory.value / "src/main/resources/diagram")
      val inputCLIParam = inputs.map(_.getAbsolutePath).mkString(" ")
      val output = (resourceManaged in Compile).value / "diagram"
      val outputs = {
        val r = raw"\.puml$$".r
        inputs.map(f => output / r.replaceAllIn(f.getName, ".png"))
      }
      outputs.foreach(println)
      IO.createDirectory(output)

      Process(s"plantuml  -o $output $inputCLIParam ").lines.foreach(println)

      outputs.toSeq

  }.getOrElse(Nil)
}


