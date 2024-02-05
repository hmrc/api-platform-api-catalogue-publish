import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import bloop.integrations.sbt.BloopDefaults

val appName = "api-platform-api-catalogue-publish"

val silencerVersion = "1.7.0"

scalaVersion := "2.13.12"

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies(),
    Test / unmanagedSourceDirectories += baseDirectory(_ / "test-common").value,
    scalacOptions += "-Wconf:src=routes/.*:s"
  )
  .settings(ScoverageSettings())
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(scalafixConfigSettings(IntegrationTest))
  .settings(inConfig(IntegrationTest)(BloopDefaults.configSettings))
  .settings(
    Defaults.itSettings,
    IntegrationTest / Keys.fork         := false,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "it").value,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "test-common").value,
    IntegrationTest / unmanagedResourceDirectories += baseDirectory(_ / "it" / "resources").value,
    IntegrationTest / managedClasspath += (Assets / packageBin).value
  )
  .settings(headerSettings(IntegrationTest) ++ automateHeaderSettings(IntegrationTest))
  .settings(
    scalacOptions ++= Seq(
      "-Wconf:cat=unused&src=views/.*\\.scala:s",
      "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
      "-Wconf:cat=unused&src=.*Routes\\.scala:s",
      "-Wconf:cat=unused&src=.*ReverseRoutes\\.scala:s"
    )
  )
  .settings(
      routesImport ++= Seq(
        "uk.gov.hmrc.apicataloguepublish.controllers.binders._"
      )
  )

commands ++= Seq(
  Command.command("run-all-tests") { state => "test" :: "it:test" :: state },

  Command.command("clean-and-test") { state => "clean" :: "compile" :: "run-all-tests" :: state },

  // Coverage does not need compile !
  Command.command("pre-commit") { state => "clean" :: "scalafmtAll" :: "scalafixAll" :: "coverage" :: "run-all-tests" :: "coverageOff" :: "coverageAggregate" :: state }
)
