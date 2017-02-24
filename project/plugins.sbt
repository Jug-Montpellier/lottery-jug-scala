resolvers += Resolver.url(
  "bintray-jug-montpellier",
  url("https://dl.bintray.com/jug-montpellier/sbt-plugin-releases/")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.3")

addSbtPlugin("org.jug-montpellier" % "sbt-plantuml-plugin" % "0.0.1")

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "0.5.6")
