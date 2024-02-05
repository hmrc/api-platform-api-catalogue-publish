resolvers += Resolver.typesafeRepo("releases")
resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += "Mulesoft" at "https://repository-master.mulesoft.org/nexus/content/repositories/releases"

addSbtPlugin("uk.gov.hmrc"       %  "sbt-auto-build"         % "3.20.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-distributables"     % "2.5.0")
addSbtPlugin("org.playframework" %  "sbt-plugin"             % "3.0.1")
addSbtPlugin("org.scoverage"     %  "sbt-scoverage"          % "2.0.9")
addSbtPlugin("org.scalastyle"    %% "scalastyle-sbt-plugin"  % "1.0.0")
addSbtPlugin("org.scalameta"     %  "sbt-scalafmt"           % "2.5.2")
addSbtPlugin("ch.epfl.scala"     %% "sbt-scalafix"           % "0.11.1")
addSbtPlugin("ch.epfl.scala"     %  "sbt-bloop"              % "1.5.13")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
