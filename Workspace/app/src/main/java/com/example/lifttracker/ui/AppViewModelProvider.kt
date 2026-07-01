package com.example.lifttracker.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lifttracker.LiftTrackerApplication
import com.example.lifttracker.ui.viewModels.AnalyticsViewModel
import com.example.lifttracker.ui.viewModels.CalendarViewModel
import com.example.lifttracker.ui.viewModels.DrawerViewModel
import com.example.lifttracker.ui.viewModels.LiftsViewModel
import com.example.lifttracker.ui.viewModels.MuscleGroupsViewModel
import com.example.lifttracker.ui.viewModels.RecordSessionViewModel
import com.example.lifttracker.ui.viewModels.SessionsViewModel
import com.example.lifttracker.ui.viewModels.SettingsViewModel
import com.example.lifttracker.ui.viewModels.ToolsViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire LiftTracker App
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // initializer for MuscleGroupsViewModel
        initializer {
            MuscleGroupsViewModel(
                profileRepository = liftTrackerApplication().container.profileRepository,
                muscleGroupRepository = liftTrackerApplication().container.muscleGroupRepository
            )
        }

        // initializer for LiftsViewModel
        initializer {
            LiftsViewModel(
                liftRepository = liftTrackerApplication().container.liftRepository,
                muscleGroupRepository = liftTrackerApplication().container.muscleGroupRepository,
                unitRepository = liftTrackerApplication().container.unitRepository
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
 * [com.example.lifttracker.LiftTrackerApplication]
 */
fun CreationExtras.liftTrackerApplication(): LiftTrackerApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LiftTrackerApplication)