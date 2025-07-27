addSbtPlugin("io.spray"     % "sbt-revolver" % "0.9.1")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.34")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")

val sbtDevOopsVersion = "3.2.1"
addSbtPlugin("io.kevinlee" %% "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" %% "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" %% "sbt-devoops-starter"   % sbtDevOopsVersion)
