package github.tom2433.lifttracker.data.structures

import androidx.room.Embedded
import github.tom2433.lifttracker.data.lift.Lift

data class LiftSearchDetail(
    @Embedded val liftObj: Lift,
    val muscleGroupName: String,
    val metricType: String,
    val unitName: String,
    val selected: Boolean,
    val visible: Boolean
)
