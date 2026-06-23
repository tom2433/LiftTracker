package com.example.lifttracker.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lifttracker.LiftTrackerApplication

/**
 * Provides Factory to create instance of ViewModel for the entire LiftTracker App
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // initializers here; not yet implemented
    }
}

/**
 * Extension function to queries for Application object and returns an instance of
 * [com.example.lifttracker.LiftTrackerApplication]
 */
fun CreationExtras.liftTrackerApplication(): LiftTrackerApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LiftTrackerApplication)