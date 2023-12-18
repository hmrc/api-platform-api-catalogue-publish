import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._


object AppDependencies {

  lazy val scalaCheckVersion = "1.14.0"
  lazy val jacksonVersion = "2.12.6"
  lazy val bootstrapVersion = "7.19.0"
  val apiDomainVersion = "0.11.0"
  val commonDomainVersion = "0.10.0"

  val compile = Seq(
    "uk.gov.hmrc"                       %% "bootstrap-backend-play-28"      % bootstrapVersion,
    "com.typesafe.play"                 %% "play-json"                      % "2.9.2",
    "com.typesafe.play"                 %% "play-json-joda"                 % "2.9.2",
    "org.typelevel"                     %% "cats-core"                      % "2.4.2",
    "io.swagger.parser.v3"              %  "swagger-parser"                 % "2.1.9"
      excludeAll(
      ExclusionRule("com.fasterxml.jackson.core", "jackson-databind"),
      ExclusionRule("com.fasterxml.jackson.core", "jackson-core"),
      ExclusionRule("com.fasterxml.jackson.core", "jackson-annotations"),
      ExclusionRule("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml"),
      ExclusionRule("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310")
    ),
//    "org.scala-lang.modules"            % "scala-xml_2.13"                  % "1.3.0",
    "com.fasterxml.jackson.module"      %% "jackson-module-scala"           % jacksonVersion,
    "com.fasterxml.jackson.core"        % "jackson-annotations"             % jacksonVersion,
    "com.fasterxml.jackson.core"        % "jackson-databind"                % jacksonVersion,
    "com.fasterxml.jackson.core"        % "jackson-core"                    % jacksonVersion,
    "com.fasterxml.jackson.dataformat"  % "jackson-dataformat-yaml"         % jacksonVersion,
    "com.fasterxml.jackson.datatype"    % "jackson-datatype-jsr310"         % jacksonVersion,
    "org.apache.httpcomponents"         % "httpclient"                      % "4.3.1",
    "org.apache.httpcomponents"         % "httpmime"                        % "4.3.1",
    "uk.gov.hmrc"                       %% "api-platform-api-domain"        % apiDomainVersion
  )

  val test = Seq(
    "uk.gov.hmrc"                 %% "bootstrap-test-play-28"             % bootstrapVersion,
    "org.mockito"                 %% "mockito-scala-scalatest"           % "1.17.29",
    "org.scalatest"               %% "scalatest"                         % "3.2.17",
    "com.vladsch.flexmark"        %  "flexmark-all"                      % "0.36.8",
    "com.github.tomakehurst"      %  "wiremock-jre8-standalone"          % "2.31.0",
    "uk.gov.hmrc"                 %% "api-platform-test-common-domain"   % commonDomainVersion,
  ).map(_ % "test, it")
}
