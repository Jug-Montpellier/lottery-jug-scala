classpathTypes += "maven-plugin"
resolvers += Resolver.url(
  "bintray-jug-montpellier",
  url("https://dl.bintray.com/metabookmarks/sbt-plugin-releases/")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.2-RC2")

addSbtPlugin("io.metabookmarks" % "sbt-plantuml-plugin" % "0.0.12")

//addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.1.0")
addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.10")

