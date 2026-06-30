package com.example.lifttracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lifttracker.ui.screens.AnalyticsDestination
import com.example.lifttracker.ui.screens.AnalyticsScreen
import com.example.lifttracker.ui.screens.CalendarDestination
import com.example.lifttracker.ui.screens.CalendarScreen
import com.example.lifttracker.ui.screens.MuscleGroupsDestination
import com.example.lifttracker.ui.screens.MuscleGroupsScreen
import com.example.lifttracker.ui.screens.RecordSessionDestination
import com.example.lifttracker.ui.screens.RecordSessionScreen
import com.example.lifttracker.ui.screens.SessionsDestination
import com.example.lifttracker.ui.screens.SessionsScreen
import com.example.lifttracker.ui.screens.SettingsDestination
import com.example.lifttracker.ui.screens.SettingsScreen
import com.example.lifttracker.ui.screens.ToolsDestination
import com.example.lifttracker.ui.screens.ToolsScreen

/**
 * Provides Navigation Graph for the application
 */
@Composable
fun LiftTrackerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MuscleGroupsDestination.route,
        modifier = modifier
    ) {
        // Composable for muscle groups screen (home)
        composable(route = MuscleGroupsDestination.route) {
            MuscleGroupsScreen()
        }

        // Composable for Record Session Screen
        composable(route = RecordSessionDestination.route) {
            RecordSessionScreen()
        }

        // Composable for Sessions Screen
        composable(route = SessionsDestination.route) {
            SessionsScreen()
        }

        // Composable for Calendar Screen
        composable(route = CalendarDestination.route) {
            CalendarScreen()
        }

        // Composable for Analytics Screen
        composable(route = AnalyticsDestination.route) {
            AnalyticsScreen()
        }

        // Composable for Tools Screen
        composable(route = ToolsDestination.route) {
            ToolsScreen()
        }

        // Composable for Settings Screen
        composable(route = SettingsDestination.route) {
            SettingsScreen()
        }
    }
}