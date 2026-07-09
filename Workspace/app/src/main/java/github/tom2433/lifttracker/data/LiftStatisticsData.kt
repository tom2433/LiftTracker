package github.tom2433.lifttracker.data

// This immutable projection holds every database value needed to render one lift-statistics timeframe. - Codex
data class LiftStatisticsData(
    // This is the number of sets recorded for the selected lift inside the requested timeframe. - Codex
    val liftSetCount: Int,
    // This counts only sessions in which at least one set of the selected lift was performed. - Codex
    val liftSessionCount: Int,
    // This is the active profile's total set volume and supplies the overall-percentage denominator. - Codex
    val overallSetCount: Int,
    // This is the selected muscle group's total set volume and supplies its percentage denominator. - Codex
    val muscleGroupSetCount: Int,
    // Room returns null when no position-one metrics exist, allowing the ViewModel to display a zero average. - Codex
    val averageWeight: Double?,
    // Room returns null when no position-two rep or time metrics exist in the requested timeframe. - Codex
    val averageSecondMetric: Double?,
    // The current muscle-group name is included so the map key follows lift moves reactively. - Codex
    val muscleGroupName: String,
    // The current unit name is included so the average-weight suffix follows lift edits reactively. - Codex
    val unitName: String,
    // Metric type one represents reps; every other value is rendered as elapsed time per the screen contract. - Codex
    val metricType: Int,
    // This lifetime maximum ISO date is independent of the requested statistics timeframe. - Codex
    val lastDateTrained: String?
)
