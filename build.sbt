ThisBuild / organization := "io.kevinlee"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := props.ScalaVersion

ThisBuild / conflictManager := ConflictManager.strict

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
    libraryDependencies ++= libs.all,
    dependencyOverrides ++= libs.overrideAll,
  )

lazy val props = new {

  val ProjectName = "effectie2-examples"

  val SonatypeCredentialHost = "s01.oss.sonatype.org"
  val SonatypeRepository     = s"https://$SonatypeCredentialHost/service/local"

  val ScalaVersion = "2.13.10"

  val CatsVersion = "2.9.0"

  val CatsEffect3Version = "3.4.5"

  val Fs2Version = "3.3.0"

  val NewtypeVersion = "0.4.4"
  val RefinedVersion = "0.10.1"

  val KittensVersion = "3.0.0"

  val Http4sVersion = "0.23.18"

  val PureConfigVersion = "0.17.2"

  val CirceVersion = "0.14.3"

  val DoobieVersion = "1.0.0-RC2"

  val ShapelessVersion = "2.3.10"

  val Ip4sCoreVersion = "3.2.0"

  val LogbackVersion = "1.4.1"

  val SvmSubsVersion = "20.2.0"

  val Effectie2Version = "2.0.0-beta5"
  val LoggerFVersion   = "2.0.0-beta6"

  val HedgehogVersion = "0.10.1"

  val HedgehogExtraVersion = "0.2.0"

  val ExtrasVersion = "0.28.0"

  val Slf4jApiVersion = "2.0.6"
}

lazy val libs = new {

  lazy val catsCore = "org.typelevel" %% "cats-core" % props.CatsVersion
  lazy val catsFree = "org.typelevel" %% "cats-free" % props.CatsVersion
  lazy val catsAll  = List(
    catsCore,
    catsFree
  )

  lazy val catsEffect3       = "org.typelevel" %% "cats-effect"        % props.CatsEffect3Version
  lazy val catsEffect3Kernel = "org.typelevel" %% "cats-effect-kernel" % props.CatsEffect3Version
  lazy val catsEffect3Std    = "org.typelevel" %% "cats-effect-std"    % props.CatsEffect3Version
  lazy val catsEffect3All    = List(
    catsEffect3,
    catsEffect3Kernel,
    catsEffect3Std
  )

  lazy val fs2Core = "co.fs2" %% "fs2-core" % props.Fs2Version

  lazy val newtype = "io.estatico" %% "newtype" % props.NewtypeVersion

  lazy val refined = List(
    "eu.timepit" %% "refined"            % props.RefinedVersion,
    "eu.timepit" %% "refined-cats"       % props.RefinedVersion,
//    "eu.timepit" %% "refined-eval"            % props.RefinedVersion,
    "eu.timepit" %% "refined-pureconfig" % props.RefinedVersion,
  )

  lazy val kittens = "org.typelevel" %% "kittens" % props.KittensVersion

  lazy val effectie2Ce = "io.kevinlee" %% "effectie-cats-effect" % props.Effectie2Version

  lazy val effectie2Ce3 = "io.kevinlee" %% "effectie-cats-effect3" % props.Effectie2Version

  lazy val loggerFCats  = "io.kevinlee" %% "logger-f-cats"  % props.LoggerFVersion
  lazy val loggerFLog4s = "io.kevinlee" %% "logger-f-log4s" % props.LoggerFVersion
  lazy val loggerFSlf4J = "io.kevinlee" %% "logger-f-slf4j" % props.LoggerFVersion

  lazy val http4s = List(
    "org.http4s" %% "http4s-core"         % props.Http4sVersion,
    "org.http4s" %% "http4s-ember-server" % props.Http4sVersion,
    "org.http4s" %% "http4s-ember-client" % props.Http4sVersion,
    "org.http4s" %% "http4s-circe"        % props.Http4sVersion,
    "org.http4s" %% "http4s-dsl"          % props.Http4sVersion
  )

  lazy val pureConfig = List(
    "com.github.pureconfig" %% "pureconfig"        % props.PureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-core"   % props.PureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-http4s" % props.PureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-ip4s"   % props.PureConfigVersion,
  )

  lazy val circeCore    = "io.circe" %% "circe-core"    % props.CirceVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % props.CirceVersion
  lazy val circeParser  = "io.circe" %% "circe-parser"  % props.CirceVersion
  lazy val circeLiteral = "io.circe" %% "circe-literal" % props.CirceVersion
  lazy val circeRefined = "io.circe" %% "circe-refined" % props.CirceVersion
  lazy val circeAll     = List(
    circeCore,
    circeGeneric,
    circeRefined,
    circeParser  % Test,
    circeLiteral % Test,
  )

  lazy val doobie = List(
    "org.tpolecat" %% "doobie-core"   % props.DoobieVersion,
//    "org.tpolecat" %% "doobie-postgres" % props.DoobieVersion,
    "org.tpolecat" %% "doobie-h2"     % props.DoobieVersion,
    "org.tpolecat" %% "doobie-specs2" % props.DoobieVersion
  )

  lazy val shapeless = "com.chuusai" %% "shapeless" % props.ShapelessVersion

  lazy val ip4sCore = "com.comcast" %% "ip4s-core" % props.Ip4sCoreVersion

  lazy val logback = "ch.qos.logback" % "logback-classic" % props.LogbackVersion % Runtime
  lazy val svmSubs = "org.scalameta" %% "svm-subs"        % props.SvmSubsVersion

  lazy val extrasCats          = "io.kevinlee" %% "extras-cats"           % props.ExtrasVersion
  lazy val extrasRefinement    = "io.kevinlee" %% "extras-refinement"     % props.ExtrasVersion
  lazy val extrasTypeInfo      = "io.kevinlee" %% "extras-type-info"      % props.ExtrasVersion
  lazy val extrasRender        = "io.kevinlee" %% "extras-render"         % props.ExtrasVersion
  lazy val extrasFs2V3Text     = "io.kevinlee" %% "extras-fs2-v3-text"    % props.ExtrasVersion
  lazy val extrasHedgehogCe3   = "io.kevinlee" %% "extras-hedgehog-ce3"   % props.ExtrasVersion % Test
  lazy val extrasHedgehogCirce = "io.kevinlee" %% "extras-hedgehog-circe" % props.ExtrasVersion % Test
  lazy val extrasAll           = List(
    extrasCats,
    extrasRefinement,
    extrasTypeInfo,
    extrasRender,
    extrasFs2V3Text,
    extrasHedgehogCe3,
    extrasHedgehogCirce
  )

  lazy val slf4jApi = "org.slf4j" % "slf4j-api" % props.Slf4jApiVersion

  lazy val hedgehog = List(
    "qa.hedgehog" %% "hedgehog-core"   % props.HedgehogVersion,
    "qa.hedgehog" %% "hedgehog-runner" % props.HedgehogVersion,
    "qa.hedgehog" %% "hedgehog-sbt"    % props.HedgehogVersion,
  )
    .map(_ % Test)

  lazy val hedgehogExtra = List(
    "io.kevinlee" %% "hedgehog-extra-core"    % props.HedgehogExtraVersion,
    "io.kevinlee" %% "hedgehog-extra-refined" % props.HedgehogExtraVersion,
  ).map(_ % Test)

  lazy val all = List(
    fs2Core,
    newtype,
    kittens,
    effectie2Ce3,
    loggerFCats,
    loggerFLog4s,
    loggerFSlf4J,
    shapeless,
    ip4sCore,
    slf4jApi,
  ) ++
    extrasAll ++
    catsAll ++
    catsEffect3All ++
    circeAll ++
    refined ++
    http4s ++
    doobie ++
    pureConfig

  lazy val overrideAll = all ++ hedgehog
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
      ) ++ libs.hedgehog ++ libs.hedgehogExtra,
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
