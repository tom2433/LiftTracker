package github.tom2433.lifttracker.data.structures

data class SessionDetail(
    val sessionId: Int,
    val sessionName: String,
    val sessionNote: String,
    val sessionDateIso: String,
    val visible: Boolean,
    val selected: Boolean,
    val liftSetCountPerMuscleGroupList: List<LiftSetCountPerMuscleGroup>
)

data class SessionDetailData(
    val sessionId: Int,
    val sessionName: String,
    val sessionNote: String,
    val sessionDate: String,
    val visible: Boolean,
    val selected: Boolean
)

data class SessionMuscleGroupCountData(
    val sessionId: Int,
    val muscleGroupName: String,
    val setCount: Int
)
