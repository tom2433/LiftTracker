package github.tom2433.lifttracker.data

import android.content.Context
import github.tom2433.lifttracker.data.lift.LiftRepository
import github.tom2433.lifttracker.data.lift.OfflineLiftRepository
import github.tom2433.lifttracker.data.liftday.LiftDayRepository
import github.tom2433.lifttracker.data.liftday.OfflineLiftDayRepository
import github.tom2433.lifttracker.data.liftset.LiftSetRepository
import github.tom2433.lifttracker.data.liftset.OfflineLiftSetRepository
import github.tom2433.lifttracker.data.liftunit.LiftUnitRepository
import github.tom2433.lifttracker.data.liftunit.OfflineLiftUnitRepository
import github.tom2433.lifttracker.data.musclegroup.MuscleGroupRepository
import github.tom2433.lifttracker.data.musclegroup.OfflineMuscleGroupRepository
import github.tom2433.lifttracker.data.profile.OfflineProfileRepository
import github.tom2433.lifttracker.data.profile.ProfileRepository
import github.tom2433.lifttracker.data.setmetric.OfflineSetMetricRepository
import github.tom2433.lifttracker.data.setmetric.SetMetricRepository

/**
 * App Container for dependency injection
 */
interface AppContainer {
    val liftRepository: LiftRepository
    val liftDaysRepository: LiftDayRepository
    val liftSetRepository: LiftSetRepository
    val muscleGroupRepository: MuscleGroupRepository
    val profileRepository: ProfileRepository
    val setMetricRepository: SetMetricRepository
    val liftUnitRepository: LiftUnitRepository
}

/**
 * [AppContainer] implementation that provides instance of [github.tom2433.lifttracker.data.liftday.OfflineLiftDayRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    override val liftRepository: LiftRepository by lazy {
        OfflineLiftRepository(LiftTrackerDatabase.getDatabase(context).liftDao())
    }

    /**
     * Implementation for [LiftDayRepository]
     */
    override val liftDaysRepository: LiftDayRepository by lazy {
        OfflineLiftDayRepository(LiftTrackerDatabase.getDatabase(context).liftDayDao())
    }

    override val liftSetRepository: LiftSetRepository by lazy {
        OfflineLiftSetRepository(LiftTrackerDatabase.getDatabase(context).liftSetDao())
    }

    override val muscleGroupRepository: MuscleGroupRepository by lazy {
        OfflineMuscleGroupRepository(LiftTrackerDatabase.getDatabase(context).muscleGroupDao())
    }

    override val profileRepository: ProfileRepository by lazy {
        OfflineProfileRepository(LiftTrackerDatabase.getDatabase(context).profileDao())
    }

    override val setMetricRepository: SetMetricRepository by lazy {
        OfflineSetMetricRepository(LiftTrackerDatabase.getDatabase(context).setMetricDao())
    }

    override val liftUnitRepository: LiftUnitRepository by lazy {
        OfflineLiftUnitRepository(LiftTrackerDatabase.getDatabase(context).liftUnitDao())
    }
}
