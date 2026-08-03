package github.tom2433.lifttracker.data.structures

import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.setmetric.SetMetric

data class SetCardData(
    val liftSet: LiftSet,
    val weightMetric: SetMetric,
    val secondMetric: SetMetric,
    val selected: Boolean
)