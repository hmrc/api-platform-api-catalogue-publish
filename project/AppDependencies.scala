import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

 
object AppDependencies {

  lazy val scalaCheckVersion = "1.14.0"
  lazy val enumeratumVersion = "1.6.2"
  lazy val jacksonVersion = "2.12.2"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.12.0",
    "com.typesafe.play"                 %% "play-json"                      % "2.9.2",
    "com.typesafe.play"                 %% "play-json-joda"                 % "2.9.2",
    "com.beachape"                      %% "enumeratum-play-json"           % enumeratumVersion,
    "org.raml"                          % "webapi-parser"                  % "0.5.0",
//    "com.fasterxml.jackson.core"        % "jackson-annotations"             % jacksonVersion,
//    "com.fasterxml.jackson.core"        % "jackson-databind"                % jacksonVersion,
//    "com.fasterxml.jackson.dataformat"  % "jackson-dataformat-yaml"         % jacksonVersion,
    "org.typelevel"                     %% "cats-core"                      % "2.4.2"
  )

  val test = Seq(
  "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % "5.12.0"            % Test,
  "org.pegdown"             %  "pegdown"                  % "1.6.0"             % "test, it",
  "com.typesafe.play"       %% "play-test"                % current             % Test,
  "org.mockito"             %% "mockito-scala-scalatest"  % "1.7.1"             % "test, it",
  "com.vladsch.flexmark"    %  "flexmark-all"             % "0.36.8"            % "test, it",
  "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.0.0"             % "test, it",
  "com.github.tomakehurst"  % "wiremock-jre8-standalone"  % "2.27.1"            % "test, it",
  "org.scalacheck"          %% "scalacheck"               % scalaCheckVersion   % "test, it"
  )
}
