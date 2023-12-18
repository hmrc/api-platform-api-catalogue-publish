import scoverage.ScoverageKeys

object ScoverageSettings {
  def apply() = Seq(
      // Semicolon-separated list of regexs matching classes to exclude
      ScoverageKeys.coverageExcludedPackages := Seq(
        """.*\.apidefinition\.models\..*""",
        """.*\.domain\.models\..*""",
        """uk\.gov\.hmrc\.BuildInfo""",
        """.*\.Routes;.*\.RoutesPrefix""",
        """.*\.Reverse[^.]*"""
       ).mkString(";"),
      ScoverageKeys.coverageMinimumStmtTotal   := 94.7,
      ScoverageKeys.coverageMinimumBranchTotal := 90.5,
      ScoverageKeys.coverageFailOnMinimum      := true,
      ScoverageKeys.coverageHighlighting       := true
  )

}
