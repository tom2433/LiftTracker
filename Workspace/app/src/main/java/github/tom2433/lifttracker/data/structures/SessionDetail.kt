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
