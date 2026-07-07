package github.tom2433.lifttracker.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import github.tom2433.lifttracker.LiftTrackerApplication
import github.tom2433.lifttracker.data.Lift
import github.tom2433.lifttracker.ui.viewModels.LiftScreenViewModel
import github.tom2433.lifttracker.ui.viewModels.AnalyticsViewModel
import github.tom2433.lifttracker.ui.viewModels.CalendarViewModel
import github.tom2433.lifttracker.ui.viewModels.DrawerViewModel
import github.tom2433.lifttracker.ui.viewModels.LiftsViewModel
import github.tom2433.lifttracker.ui.viewModels.MuscleGroupsViewModel
import github.tom2433.lifttracker.ui.viewModels.RecordSessionViewModel
import github.tom2433.lifttracker.ui.viewModels.SessionsViewModel
import github.tom2433.lifttracker.ui.viewModels.SettingsViewModel
import github.tom2433.lifttracker.ui.viewModels.ToolsViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire LiftTracker App
 */
object AppViewModelProvider {
    fun liftsFactory(muscleGroupId: Int) = viewModelFactory {
        initializer {
            LiftsViewModel(
                muscleGroupId = muscleGroupId,
                liftRepository = liftTrackerApplication().container.liftRepository,
                muscleGroupRepository = liftTrackerApplication().container.muscleGroupRepository,
                unitRepository = liftTrackerApplication().container.unitRepository
            )
        }
    }

    fun liftScreenFactory(lift: Lift) = viewModelFactory {
        initializer {
            LiftScreenViewModel(
                lift = lift,
                liftRepository = liftTrackerApplication().container.liftRepository,
                muscleGroupRepository = liftTrackerApplication().container.muscleGroupRepository,
                unitRepository = liftTrackerApplication().container.unitRepository
            )
        }
    }

    val Factory = viewModelFactory {
        // initializer for MuscleGroupsViewModel
        initializer {
            MuscleGroupsViewModel(
                profileRepository = liftTrackerApplication().container.profileRepository,
                muscleGroupRepository = liftTrackerApplication().container.muscleGroupRepository
            )
        }

        // initializer for AnalyticsViewModel
        initializer {
            AnalyticsViewModel(
                profileRepository = liftTrackerApplication().container.profileRepository
            )
        }

        // initializer for CalendarViewModel
        initializer {
            CalendarViewModel(
                profileRepository = liftTrackerApplication().container.profileRepository
            )
        }

        // initializer for RecordSessionViewModel
        initializer {
            RecordSessionViewModel(
                profileRepository = liftTrackerApplication().container.profileRepository
            )
        }

        // initializer for SessionsViewModel
        initializer {
            SessionsViewModel(
                profileRepository = liftTrackerApplication().container.profileRepository
            )
        }

        // initializer for SettingsViewModel
        initializer {
            SettingsViewModel(
                profileRepository = liftTrackerApplication().container.profileRepository
            )
        }

        // initializer for ToolsViewModel
        initializer {
            ToolsViewModel(
                profileRepository = liftTrackerApplication().container.profileRepository
            )
        }

        // initializer for DrawerViewModel
        initializer {
            DrawerViewModel(
                profileRepository = liftTrackerApplication().container.profileRepository,
                muscleGroupRepository = liftTrackerApplication().container.muscleGroupRepository
            )
        }
    }
}

/**
 * Extension function to queries for Application object and returns an instance of
 * [github.tom2433.lifttracker.LiftTrackerApplication]
 */
fun CreationExtras.liftTrackerApplication(): LiftTrackerApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LiftTrackerApplication)