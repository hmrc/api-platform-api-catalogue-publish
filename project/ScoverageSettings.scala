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
      ScoverageKeys.coverageMinimumStmtTotal   := 94.6,
      ScoverageKeys.coverageMinimumBranchTotal := 89.7,
      ScoverageKeys.coverageFailOnMinimum      := true,
      ScoverageKeys.coverageHighlighting       := true
  )

}
