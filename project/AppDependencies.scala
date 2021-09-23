import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

 
object AppDependencies {

  lazy val scalaCheckVersion = "1.14.0"
  lazy val enumeratumVersion = "1.6.2"
  lazy val jacksonVersion = "2.11.1"

  val compile = Seq(
    "uk.gov.hmrc"                       %% "bootstrap-backend-play-28"      % "5.14.0",
    "com.typesafe.play"                 %% "play-json"                      % "2.9.2",
    "com.typesafe.play"                 %% "play-json-joda"                 % "2.9.2",
    "com.beachape"                      %% "enumeratum-play-json"           % enumeratumVersion,
    "org.raml"                          % "webapi-parser"                   % "0.5.0",
    "org.typelevel"                     %% "cats-core"                      % "2.4.2",
    "io.swagger.parser.v3"              % "swagger-parser-v3"               % "2.0.24",
    "com.fasterxml.jackson.module"      %% "jackson-module-scala"           % jacksonVersion,
    "com.fasterxml.jackson.core"        % "jackson-annotations"             % jacksonVersion,
    "com.fasterxml.jackson.core"        % "jackson-databind"                % jacksonVersion,
    "com.fasterxml.jackson.core"        % "jackson-core"                    % jacksonVersion,
    "com.fasterxml.jackson.dataformat"  % "jackson-dataformat-yaml"         % jacksonVersion
  )

  val test = Seq(
    "uk.gov.hmrc"                 %% "bootstrap-test-play-28"             % "5.14.0"      % "test, it",
    "org.mockito"                 %% "mockito-scala-scalatest"            % "1.16.42"     % "test, it",
    "com.github.tomakehurst"      %  "wiremock-jre8-standalone"           % "2.27.1"      % "test, it"         
  )
}