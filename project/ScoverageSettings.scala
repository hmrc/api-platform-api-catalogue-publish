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
      ScoverageKeys.coverageMinimumStmtTotal   := 93.25,
      ScoverageKeys.coverageMinimumBranchTotal := 88.4,
      ScoverageKeys.coverageFailOnMinimum      := true,
      ScoverageKeys.coverageHighlighting       := true
  )

}
