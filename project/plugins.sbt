addSbtPlugin("com.timushev.sbt"  % "sbt-updates" % "0.3.4")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1")
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"     % "1.0.0")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
