resolvers += Classpaths.sbtPluginSnapshots

addSbtPlugin("org.github.jopecko" % "sbt-simple-url-update" % sys.props("project.version"))
