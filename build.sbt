import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import bloop.integrations.sbt.BloopDefaults

val appName = "api-platform-api-catalogue-publish"

val silencerVersion = "1.7.0"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.12.12",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    Test / unmanagedSourceDirectories += baseDirectory(_ / "test-common").value,
    // ***************
    // Use the silencer plugin to suppress warnings
    scalacOptions ++= Seq(
      "-P:silencer:pathFilters=routes",
      "-Ypartial-unification"
    ),
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
    // ***************
  )
  .settings(publishingSettings,
    scoverageSettings)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(inConfig(IntegrationTest)(BloopDefaults.configSettings))
  .settings(
    Defaults.itSettings,
    IntegrationTest / Keys.fork := false,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "it").value,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "test-common").value,
    IntegrationTest / unmanagedResourceDirectories += baseDirectory(_ / "it" / "resources").value,
    (managedClasspath in IntegrationTest) += (packageBin in Assets).value
  )
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := ";.*\\.apidefinition\\.models\\..*;.*\\.domain\\.models\\..*;uk\\.gov\\.hmrc\\.BuildInfo;.*\\.Routes;.*\\.RoutesPrefix;;.*ConfigurationModule;GraphiteStartUp;.*\\.Reverse[^.]*",
    ScoverageKeys.coverageMinimum := 96,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}