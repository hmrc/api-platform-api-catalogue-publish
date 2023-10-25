import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import bloop.integrations.sbt.BloopDefaults

val appName = "api-platform-api-catalogue-publish"

val silencerVersion = "1.7.0"

scalaVersion := "2.13.8"

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    Test / unmanagedSourceDirectories += baseDirectory(_ / "test-common").value
  )
  .settings(scoverageSettings)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
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
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages   := ";.*\\.apidefinition\\.models\\..*;.*\\.domain\\.models\\..*;uk\\.gov\\.hmrc\\.BuildInfo;.*\\.Routes;.*\\.RoutesPrefix;;.*ConfigurationModule;GraphiteStartUp;.*\\.Reverse[^.]*",
    ScoverageKeys.coverageMinimumStmtTotal   := 96,
    ScoverageKeys.coverageMinimumBranchTotal := 95,
    ScoverageKeys.coverageFailOnMinimum      := true,
    ScoverageKeys.coverageHighlighting       := true,
    Test / parallelExecution                 := false
  )
}


commands ++= Seq(
  Command.command("run-all-tests") { state => "test" :: "it:test" :: state },

  Command.command("clean-and-test") { state => "clean" :: "compile" :: "run-all-tests" :: state },

  // Coverage does not need compile !
  Command.command("pre-commit") { state => "scalafmtAll" :: "scalafixAll" :: "clean" :: "coverage" :: "run-all-tests" :: "coverageReport" :: state }
)