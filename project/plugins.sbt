classpathTypes += "maven-plugin"
resolvers += Resolver.url(
  "bintray-jug-montpellier",
  url("https://dl.bintray.com/metabookmarks/sbt-plugin-releases/")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.5.2")

addSbtPlugin("io.metabookmarks" % "sbt-plantuml-plugin" % "0.0.51")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.3.2")