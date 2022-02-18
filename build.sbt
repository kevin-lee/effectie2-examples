ThisBuild / organization := "io.kevinlee"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

lazy val effectieExamples = (project in file("."))
  .settings(
    name := props.ProjectName
  )
  .aggregate(effectieCe3)

ThisBuild / resolvers += "sonatype-snapshots" at s"https://${props.SonatypeCredentialHost}/content/repositories/snapshots"

updateOptions := updateOptions.value.withLatestSnapshots(true)
//updateOptions := updateOptions.value.withCachedResolution(false)

lazy val effectieCe3 = subProject("cats-effect3")
  .settings(
    libraryDependencies ++= List(
      libs.newtype,
      libs.effect2Ce3,
      libs.loggerFCats,
      libs.loggerFLog4s,
      libs.extrasHedgehogCe3,
    ) ++
      libs.refined ++
      libs.http4s ++
      libs.doobie ++
      libs.pureConfig
  )

lazy val props = new {

  val ProjectName = "effectie2-examples"

  val SonatypeCredentialHost = "s01.oss.sonatype.org"
  val SonatypeRepository     = s"https://$SonatypeCredentialHost/service/local"

  val NewtypeVersion = "0.4.4"
  val RefinedVersion = "0.9.28"

  val Http4sVersion = "0.23.7"

  val PureConfigVersion = "0.17.1"

  val CirceVersion = "0.14.1"

  val DoobieVersion = "1.0.0-RC2"

  val LogbackVersion = "1.2.10"

  val SvmSubsVersion = "20.2.0"

  val Effectie2Version = "2.0.0-SNAPSHOT"

  val LoggerFVersion = "2.0.0-SNAPSHOT"

  val HedgehogVersion = "0.8.0"

  val ExtrasVersion = "0.4.0"
}

lazy val libs = new {

  lazy val newtype = "io.estatico" %% "newtype" % props.NewtypeVersion

  lazy val refined = List(
    "eu.timepit" %% "refined"            % props.RefinedVersion,
    "eu.timepit" %% "refined-cats"       % props.RefinedVersion,
//    "eu.timepit" %% "refined-eval"            % props.RefinedVersion,
    "eu.timepit" %% "refined-pureconfig" % props.RefinedVersion,
  )

  lazy val effect2Ce = "io.kevinlee" %% "effectie-cats-effect" % props.Effectie2Version

  lazy val effect2Ce3 = "io.kevinlee" %% "effectie-cats-effect3" % props.Effectie2Version

  lazy val loggerFCats  = "io.kevinlee" %% "logger-f-cats"  % props.LoggerFVersion
  lazy val loggerFLog4s = "io.kevinlee" %% "logger-f-log4s" % props.LoggerFVersion

  lazy val http4s = List(
    "org.http4s" %% "http4s-blaze-server" % props.Http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % props.Http4sVersion,
    "org.http4s" %% "http4s-circe"        % props.Http4sVersion,
    "org.http4s" %% "http4s-dsl"          % props.Http4sVersion
  )

  lazy val pureConfig = List(
    "com.github.pureconfig" %% "pureconfig"        % props.PureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-http4s" % props.PureConfigVersion,
  )

  lazy val circe = "io.circe" %% "circe-generic" % props.CirceVersion

  lazy val doobie = List(
    "org.tpolecat" %% "doobie-core"   % props.DoobieVersion,
//    "org.tpolecat" %% "doobie-postgres" % props.DoobieVersion,
    "org.tpolecat" %% "doobie-h2"     % props.DoobieVersion,
    "org.tpolecat" %% "doobie-specs2" % props.DoobieVersion
  )

  lazy val logback = "ch.qos.logback" % "logback-classic" % props.LogbackVersion % Runtime
  lazy val svmSubs = "org.scalameta" %% "svm-subs"        % props.SvmSubsVersion

  lazy val extrasHedgehogCe3 = "io.kevinlee" %% "extras-hedgehog-cats-effect3" % props.ExtrasVersion % Test

  lazy val hedgehog = List(
    "qa.hedgehog" %% "hedgehog-core"   % props.HedgehogVersion,
    "qa.hedgehog" %% "hedgehog-runner" % props.HedgehogVersion,
    "qa.hedgehog" %% "hedgehog-sbt"    % props.HedgehogVersion,
  )
    .map(_ % Test)
}

def prefixName(name: String): String =
  if (name.isEmpty) props.ProjectName else s"${props.ProjectName}-$name"

def subProject(projectName: String): Project = {
  val prefixedName = prefixName(projectName)
  Project(prefixedName, file(s"examples/$prefixedName"))
    .settings(
      name          := prefixedName,
      libraryDependencies ++= List(
        libs.circe,
        libs.logback,
      ) ++ libs.hedgehog,
      testFrameworks += new TestFramework("hedgehog.sbt.Framework"),
      updateOptions := updateOptions.value.withLatestSnapshots(true),
      //    updateOptions := updateOptions.value.withCachedResolution(false)

    )
}
