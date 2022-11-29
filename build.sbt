ThisBuild / organization := "io.kevinlee"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := props.ScalaVersion

lazy val effectieExamples = (project in file("."))
  .settings(
    name := props.ProjectName
  )
  .aggregate(effectieCe3)

ThisBuild / resolvers += "sonatype-snapshots" at s"https://${props.SonatypeCredentialHost}/content/repositories/snapshots"
ThisBuild / scalafixConfig := (
  if (scalaVersion.value.startsWith("3")) file(".scalafix-scala3.conf").some
  else file(".scalafix-scala2.conf").some
)

updateOptions := updateOptions.value.withLatestSnapshots(true)
//updateOptions := updateOptions.value.withCachedResolution(false)

lazy val effectieCe3 = subProject("cats-effect3")
  .settings(
    libraryDependencies ++= List(
      libs.cats,
      libs.catsEffect3,
      libs.newtype,
      libs.kittens,
      libs.effect2Ce3,
      libs.loggerFCats,
      libs.loggerFLog4s,
      libs.extrasCats,
      libs.extrasRefinement,
      libs.extrasHedgehogCe3,
      libs.extrasHedgehogCirce,
      libs.circeGeneric,
      libs.circeRefined,
      libs.circeParser  % Test,
      libs.circeLiteral % Test,
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

  val ScalaVersion = "2.13.10"

  val CatsVersion = "2.9.0"

  val CatsEffect3Version = "3.4.1"

  val NewtypeVersion = "0.4.4"
  val RefinedVersion = "0.10.1"

  val KittensVersion = "3.0.0"

  val Http4sVersion = "0.23.16"

  val PureConfigVersion = "0.17.1"

  val CirceVersion = "0.14.3"

  val DoobieVersion = "1.0.0-RC2"

  val LogbackVersion = "1.4.1"

  val SvmSubsVersion = "20.2.0"

  val Effectie2Version = "2.0.0-beta3"
  val LoggerFVersion   = "2.0.0-beta3"

  val HedgehogVersion = "0.9.0"

  val ExtrasVersion = "0.25.0"
}

lazy val libs = new {

  lazy val cats        = "org.typelevel" %% "cats-core"   % props.CatsVersion
  lazy val catsEffect3 = "org.typelevel" %% "cats-effect" % props.CatsEffect3Version

  lazy val newtype = "io.estatico" %% "newtype" % props.NewtypeVersion

  lazy val refined = List(
    "eu.timepit" %% "refined"            % props.RefinedVersion,
    "eu.timepit" %% "refined-cats"       % props.RefinedVersion,
//    "eu.timepit" %% "refined-eval"            % props.RefinedVersion,
    "eu.timepit" %% "refined-pureconfig" % props.RefinedVersion,
  )

  lazy val kittens = "org.typelevel" %% "kittens" % props.KittensVersion

  lazy val effect2Ce = "io.kevinlee" %% "effectie-cats-effect" % props.Effectie2Version

  lazy val effect2Ce3 = "io.kevinlee" %% "effectie-cats-effect3" % props.Effectie2Version

  lazy val loggerFCats  = "io.kevinlee" %% "logger-f-cats"  % props.LoggerFVersion
  lazy val loggerFLog4s = "io.kevinlee" %% "logger-f-log4s" % props.LoggerFVersion

  lazy val http4s = List(
    "org.http4s" %% "http4s-ember-server" % props.Http4sVersion,
    "org.http4s" %% "http4s-ember-client" % props.Http4sVersion,
    "org.http4s" %% "http4s-circe"        % props.Http4sVersion,
    "org.http4s" %% "http4s-dsl"          % props.Http4sVersion
  )

  lazy val pureConfig = List(
    "com.github.pureconfig" %% "pureconfig"        % props.PureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-http4s" % props.PureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-ip4s"   % props.PureConfigVersion,
  )

  lazy val circeGeneric = "io.circe" %% "circe-generic" % props.CirceVersion
  lazy val circeParser  = "io.circe" %% "circe-parser"  % props.CirceVersion
  lazy val circeLiteral = "io.circe" %% "circe-literal" % props.CirceVersion
  lazy val circeRefined = "io.circe" %% "circe-refined" % props.CirceVersion

  lazy val doobie = List(
    "org.tpolecat" %% "doobie-core"   % props.DoobieVersion,
//    "org.tpolecat" %% "doobie-postgres" % props.DoobieVersion,
    "org.tpolecat" %% "doobie-h2"     % props.DoobieVersion,
    "org.tpolecat" %% "doobie-specs2" % props.DoobieVersion
  )

  lazy val logback = "ch.qos.logback" % "logback-classic" % props.LogbackVersion % Runtime
  lazy val svmSubs = "org.scalameta" %% "svm-subs"        % props.SvmSubsVersion

  lazy val extrasCats          = "io.kevinlee" %% "extras-cats"           % props.ExtrasVersion
  lazy val extrasRefinement    = "io.kevinlee" %% "extras-refinement"     % props.ExtrasVersion
  lazy val extrasHedgehogCe3   = "io.kevinlee" %% "extras-hedgehog-ce3"   % props.ExtrasVersion % Test
  lazy val extrasHedgehogCirce = "io.kevinlee" %% "extras-hedgehog-circe" % props.ExtrasVersion % Test

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
      name           := prefixedName,
      libraryDependencies ++= List(
        libs.logback,
      ) ++ libs.hedgehog,
      testFrameworks += new TestFramework("hedgehog.sbt.Framework"),
      updateOptions  := updateOptions.value.withLatestSnapshots(true),
      scalafixConfig := (
        if (scalaVersion.value.startsWith("3"))
          ((ThisBuild / baseDirectory).value / ".scalafix-scala3.conf").some
        else
          ((ThisBuild / baseDirectory).value / ".scalafix-scala2.conf").some
      ),
      //    updateOptions := updateOptions.value.withCachedResolution(false)

    )
}
