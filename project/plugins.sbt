resolvers += "Maven Central" at "https://repo1.maven.org/maven2/"

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.5")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.1")

// Publishing
//addSbtPlugin("io.get-coursier" % "sbt-coursier" % "2.1.4")
//addSbtPlugin("io.get-coursier" % "sbt-pgp-coursier" % "2.1.4")
