sbtPlugin := true

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.4")

name := "sbt-simple-url-update"
organization := "org.github.jopecko"
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
resolvers += Classpaths.sbtPluginSnapshots
scalaVersion := "2.12.8"
crossSbtVersions := Seq("0.13.17", "1.1.6")

bintrayReleaseOnPublish in ThisBuild := false
bintrayRepository := "sbt-plugins"

enablePlugins(GitVersioning)

/* The BaseVersion setting represents the in-development (upcoming) version,
 * as an alternative to SNAPSHOTS.
 */
git.baseVersion := "1.1.0"

val ReleaseTag = """^v([\d\.]+)$""".r
git.gitTagToVersionNumber := {
  case ReleaseTag(v) => Some(v)
  case _ => None
}

git.formattedShaVersion := {
  val suffix = git.makeUncommittedSignifierSuffix(
    git.gitUncommittedChanges.value,
    git.uncommittedSignifier.value
  )

  git.gitHeadCommit.value map { _.substring(0, 7) } map { sha =>
    git.baseVersion.value + "-" + sha + suffix
  }
}

scriptedSettings
scriptedLaunchOpts += s"-Dproject.version=${version.value}"
