package com.example.lifttracker.data

import android.content.Context

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
    val unitRepository: UnitRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineLiftDayRepository]
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

    override val unitRepository: UnitRepository by lazy {
        OfflineUnitRepository(LiftTrackerDatabase.getDatabase(context).unitDao())
    }
}