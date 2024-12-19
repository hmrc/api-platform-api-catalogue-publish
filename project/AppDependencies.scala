import sbt._


object AppDependencies {

  def apply(): Seq[ModuleID] = compile ++ test

  lazy val bootstrapVersion = "9.0.0"
  val apiDomainVersion = "0.19.1"
  val commonDomainVersion = "0.17.0"

  val compile = Seq(
    "uk.gov.hmrc"                       %% "bootstrap-backend-play-30"      % bootstrapVersion,
    "org.typelevel"                     %% "cats-core"                      % "2.10.0",
    "io.swagger.parser.v3"              %  "swagger-parser"                 % "2.1.14",
    "uk.gov.hmrc"                       %% "api-platform-api-domain"        % apiDomainVersion
  )

  val test = Seq(
    "uk.gov.hmrc"                 %% "bootstrap-test-play-30"                % bootstrapVersion,
    "org.mockito"                 %% "mockito-scala-scalatest"               % "1.17.29",
    "uk.gov.hmrc"                 %% "api-platform-common-domain-fixtures"   % commonDomainVersion,
  ).map(_ % "test")
}
