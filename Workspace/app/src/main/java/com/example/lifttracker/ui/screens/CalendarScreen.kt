package com.example.lifttracker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.lifttracker.LiftTrackerDrawer
import com.example.lifttracker.R
import com.example.lifttracker.ui.navigation.NavigationDestination

object CalendarDestination : NavigationDestination {
    override val route = "calendar"
    override val titleRes = R.string.calendar_screen_title
}

/**
 * Entry route for Calendar Screen
 */
@Composable
fun CalendarScreen(
    navigateToRecordSession: () -> Unit,
    navigateToMuscleGroups: () -> Unit,
    navigateToSessions: () -> Unit,
    navigateToCalendar: () -> Unit,
    navigateToAnalytics: () -> Unit,
    navigateToTools: () -> Unit,
    navigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiftTrackerDrawer(
        titleRes = CalendarDestination.titleRes,
        navigateToRecordSession = navigateToRecordSession,
        navigateToMuscleGroups = navigateToMuscleGroups,
        navigateToSessions = navigateToSessions,
        navigateToCalendar = navigateToCalendar,
        navigateToAnalytics = navigateToAnalytics,
        navigateToTools = navigateToTools,
        navigateToSettings = navigateToSettings
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Calendar Screen needs to be implemented here"
            )
        }
    }
}