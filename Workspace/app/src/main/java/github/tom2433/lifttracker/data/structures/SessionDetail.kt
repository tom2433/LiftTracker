package github.tom2433.lifttracker.data.structures

data class SessionDetail(
    val sessionId: Int,
    val sessionName: String,
    val sessionNote: String,
    val sessionDateIso: String,
    val sessionInProgress: Boolean,
    val visible: Boolean,
    val selected: Boolean,
    val menuExpanded: Boolean,
    val liftSetCountPerMuscleGroupList: List<LiftSetCountPerMuscleGroup>
)

data class SessionDetailData(
    val sessionId: Int,
    val sessionName: String,
    val sessionNote: String,
    val sessionDate: String,
    val sessionInProgress: Boolean
)

data class SessionMuscleGroupCountData(
    val sessionId: Int,
    val muscleGroupName: String,
    val setCount: Int
)

data class SessionNameAndFrequency(
    val sessionName: String,
    val sessionFrequency: Int
)

data class MuscleGroupNameAndFrequency(
    val muscleGroupName: String,
    val muscleGroupFrequency: Int
)

data class LiftNameAndFrequency(
    val liftName: String,
    val liftFrequency: Int
)

data class LiftDataVisUntimed(
    val liftId: Int,
    val weight: Double,
    val reps: Double,
    val volumePerSet: Double // = avg (reps * weight)
)

data class LiftDataVisTimed(
    val liftId: Int,
    val weight: Double,
    val mins: Double,
    val weightPerMin: Double // avg (weight / mins)
)

data class LiftSummary(
    val liftName: String,
    val timed: Boolean,
    val unitName: String,
    val paragraph: String,
    val avgWeight: Double?,
    val historicalAvgWeight: Double?,
    val avgRepsOrTime: Double?,
    val historicalAvgRepsOrTime: Double?,
    val avgIntensity: Double?,
    val historicalAvgIntensity: Double?,
    val setDistributionPoints: List<SetDistributionPoint> = listOf()
)

data class SetDistributionPoint(
    val setNumber: Int,
    val weightValue: Double,
    val repsOrTime: Double,
    val intensity: Double,
    val avgWeightValue: Double?,
    val avgRepsOrTime: Double?,
    val avgIntensity: Double?
)

data class SessionSummary(
    val paragraph1: String = "",
    val paragraph2: String = "",
    val explanation: String = "",
    val untimedDataPoints: List<SessionDataPoint> = listOf(),
    val timedDataPoints: List<SessionDataPoint> = listOf(),
    val untimedUnits: String = "units",
    val timedUnits: String = "units"
)

data class SessionDataPoint(
    val sessionId: Int,
    val sessionNote: String,
    val sessionDateIso: String,
    val unitName: String,
    val weightDeviation: Double,
    val repsOrTimeDeviation: Double,
    val intensityDeviation: Double
)
