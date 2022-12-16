import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

 
object AppDependencies {

  lazy val scalaCheckVersion = "1.14.0"
  lazy val enumeratumVersion = "1.6.3"
  lazy val jacksonVersion = "2.12.6"
  lazy val bootstrapVersion = "7.12.0"

  val compile = Seq(
    "uk.gov.hmrc"                       %% "bootstrap-backend-play-28"      % bootstrapVersion,
    "com.typesafe.play"                 %% "play-json"                      % "2.9.2",
    "com.typesafe.play"                 %% "play-json-joda"                 % "2.9.2",
    "com.beachape"                      %% "enumeratum-play-json"           % enumeratumVersion,
    "org.raml"                          % "webapi-parser"                   % "0.5.0",
    "org.typelevel"                     %% "cats-core"                      % "2.4.2",
    "io.swagger.parser.v3"              % "swagger-parser"                  % "2.1.9"
      excludeAll(
      ExclusionRule("com.fasterxml.jackson.core", "jackson-databind"),
      ExclusionRule("com.fasterxml.jackson.core", "jackson-core"),
      ExclusionRule("com.fasterxml.jackson.core", "jackson-annotations"),
      ExclusionRule("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml"),
      ExclusionRule("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310")
    ),
    "com.fasterxml.jackson.module"      %% "jackson-module-scala"           % jacksonVersion,
    "com.fasterxml.jackson.core"        % "jackson-annotations"             % jacksonVersion,
    "com.fasterxml.jackson.core"        % "jackson-databind"                % jacksonVersion,
    "com.fasterxml.jackson.core"        % "jackson-core"                    % jacksonVersion,
    "com.fasterxml.jackson.dataformat"  % "jackson-dataformat-yaml"         % jacksonVersion,
    "com.fasterxml.jackson.datatype"    % "jackson-datatype-jsr310"         % jacksonVersion,
    "org.apache.httpcomponents"         % "httpclient"                      % "4.3.1",
    "org.apache.httpcomponents"         % "httpmime"                        % "4.3.1"
  )

  val test = Seq(
    "uk.gov.hmrc"                 %% "bootstrap-test-play-28"             % bootstrapVersion    % "test, it",
    "org.mockito"                 %% "mockito-scala-scalatest"            % "1.16.42"           % "test, it",
    "com.github.tomakehurst"      %  "wiremock-jre8-standalone"           % "2.27.1"            % "test, it"
  )
}