package com.example.lifttracker.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lifttracker.LiftTrackerApplication
import com.example.lifttracker.ui.viewModels.MuscleGroupsViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire LiftTracker App
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // initializer for MuscleGroupsViewModel
        initializer {
            MuscleGroupsViewModel(
                profileRepository = liftTrackerApplication().container.profileRepository
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