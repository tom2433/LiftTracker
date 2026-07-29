package github.tom2433.lifttracker.data.structures

import androidx.room.Embedded
import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.setmetric.SetMetric

data class DisplaySessionLiftSetRow(
    @Embedded(prefix = "set_")
    val liftSet: LiftSet,

    @Embedded(prefix = "weight_")
    val weightMetric: SetMetric,

    @Embedded(prefix = "second_")
    val secondMetric: SetMetric
)