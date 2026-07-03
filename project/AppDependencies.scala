import sbt._


object AppDependencies {

  def apply(): Seq[ModuleID] = compile ++ test

  val bootstrapVersion = "10.7.0"
  val apiDomainVersion = "1.5.0"
  val commonDomainVersion = "1.1.0"
  val mockitoScalaVersion = "2.2.1"

  val compile = Seq(
    "uk.gov.hmrc"                       %% "bootstrap-backend-play-30"      % bootstrapVersion,
    "org.typelevel"                     %% "cats-core"                      % "2.13.0",
    "io.swagger.parser.v3"              %  "swagger-parser"                 % "2.1.44",
    "uk.gov.hmrc"                       %% "api-platform-api-domain"        % apiDomainVersion,
    "org.playframework"                 %% "play-json"                      % "3.1.0-M10"
  )

  val test = Seq(
    "uk.gov.hmrc"                 %% "bootstrap-test-play-30"                % bootstrapVersion,
    "org.mockito"                 %% "mockito-scala-scalatest"               % mockitoScalaVersion,
    "uk.gov.hmrc"                 %% "api-platform-common-domain-fixtures"   % commonDomainVersion
  ).map(_ % "test")
}
