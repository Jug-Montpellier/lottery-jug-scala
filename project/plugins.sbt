classpathTypes += "maven-plugin"
resolvers += Resolver.url(
  "bintray-jug-montpellier",
  url("https://dl.bintray.com/metabookmarks/sbt-plugin-releases/")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.4.1")

addSbtPlugin("io.metabookmarks" % "sbt-plantuml-plugin" % "0.0.46")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.7")