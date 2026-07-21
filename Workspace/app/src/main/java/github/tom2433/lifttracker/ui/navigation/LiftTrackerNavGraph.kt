package github.tom2433.lifttracker.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import github.tom2433.lifttracker.ui.screens.AnalyticsDestination
import github.tom2433.lifttracker.ui.screens.AnalyticsScreen
import github.tom2433.lifttracker.ui.screens.CalendarDestination
import github.tom2433.lifttracker.ui.screens.CalendarScreen
import github.tom2433.lifttracker.ui.screens.MuscleGroupsDestination
import github.tom2433.lifttracker.ui.screens.MuscleGroupsScreen
import github.tom2433.lifttracker.ui.screens.RecordSessionDestination
import github.tom2433.lifttracker.ui.screens.RecordSessionScreen
import github.tom2433.lifttracker.ui.screens.SessionsDestination
import github.tom2433.lifttracker.ui.screens.SessionsScreen
import github.tom2433.lifttracker.ui.screens.SettingsDestination
import github.tom2433.lifttracker.ui.screens.SettingsScreen
import github.tom2433.lifttracker.ui.screens.ToolsDestination
import github.tom2433.lifttracker.ui.screens.ToolsScreen

/**
 * Provides Navigation Graph for the application
 */
@RequiresApi(Build.VERSION_CODES.O)
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