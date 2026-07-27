package github.tom2433.lifttracker.data

import android.content.Context
import github.tom2433.lifttracker.data.lift.LiftRepository
import github.tom2433.lifttracker.data.lift.OfflineLiftRepository
import github.tom2433.lifttracker.data.session.SessionRepository
import github.tom2433.lifttracker.data.session.OfflineSessionRepository
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
 * Interface for App Container for dependency injection
 */
interface AppContainer {
    val liftRepository: LiftRepository
    val sessionsRepository: SessionRepository
    val liftSetRepository: LiftSetRepository
    val muscleGroupRepository: MuscleGroupRepository
    val profileRepository: ProfileRepository
    val setMetricRepository: SetMetricRepository
    val liftUnitRepository: LiftUnitRepository
}

/**
 * [AppContainer] implementation that provides instances of all repositories to inject into
 * ViewModels. This implementation requires context provided by an Application.
 *
 * @see github.tom2433.lifttracker.LiftTrackerApplication
 *
 * @param context the context provided by LiftTrackerApplication
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [LiftRepository]
     */
    override val liftRepository: LiftRepository by lazy {
        OfflineLiftRepository(LiftTrackerDatabase.getDatabase(context).liftDao())
    }

    /**
     * Implementation for [SessionRepository]
     */
    override val sessionsRepository: SessionRepository by lazy {
        OfflineSessionRepository(LiftTrackerDatabase.getDatabase(context).sessionDao())
    }

    /**
     * Implementation for [LiftSetRepository]
     */
    override val liftSetRepository: LiftSetRepository by lazy {
        OfflineLiftSetRepository(LiftTrackerDatabase.getDatabase(context).liftSetDao())
    }

    /**
     * Implementation for [MuscleGroupRepository]
     */
    override val muscleGroupRepository: MuscleGroupRepository by lazy {
        OfflineMuscleGroupRepository(LiftTrackerDatabase.getDatabase(context).muscleGroupDao())
    }

    /**
     * Implementation for [ProfileRepository]
     */
    override val profileRepository: ProfileRepository by lazy {
        OfflineProfileRepository(LiftTrackerDatabase.getDatabase(context).profileDao())
    }

    /**
     * Implementation for [SetMetricRepository]
     */
    override val setMetricRepository: SetMetricRepository by lazy {
        OfflineSetMetricRepository(LiftTrackerDatabase.getDatabase(context).setMetricDao())
    }

    /**
     * Implementation for [LiftUnitRepository]
     */
    override val liftUnitRepository: LiftUnitRepository by lazy {
        OfflineLiftUnitRepository(LiftTrackerDatabase.getDatabase(context).liftUnitDao())
    }
}
