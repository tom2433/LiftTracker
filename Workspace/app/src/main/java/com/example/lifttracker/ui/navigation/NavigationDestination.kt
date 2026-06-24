package com.example.lifttracker.ui.navigation

/**
 * Interface to describe the navigation destinations for the app
 */
interface NavigationDestination {
    // unique name to define the path for a composable
    val route: String

    // string resource id that contains title to be displayed for the screen
    val titleRes: Int
}