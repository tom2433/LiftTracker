package github.tom2433.lifttracker.data.structures

import androidx.room.Embedded
import github.tom2433.lifttracker.data.lift.Lift
import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.setmetric.SetMetric

/**
 * Row projection used to store SetMetrics for a particular Lift and also provide other data for
 * UI.
 *
 * Embedded prefixes are used to allow Room to appropriately map result columns from a query into
 * nested objects like this one.
 */
data class RecordSessionLiftSetRow(
    val liftId: Int,

    @Embedded(prefix = "set_")
    val liftSet: LiftSet,

    @Embedded(prefix = "weight_")
    val weightMetric: SetMetric,

    @Embedded(prefix = "second_")
    val secondMetric: SetMetric
)
