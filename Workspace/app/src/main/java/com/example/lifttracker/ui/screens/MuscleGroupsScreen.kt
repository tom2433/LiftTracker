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

object MuscleGroupsDestination : NavigationDestination {
    override val route = "muscleGroups"
    override val titleRes = R.string.muscle_groups_title
}

/**
 * Entry route for Home Screen
 */
@Composable
fun MuscleGroupsScreen(
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
        titleRes = MuscleGroupsDestination.titleRes,
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
                text = "Muscle groups will show up here, we also need a FAB"
            )
        }
    }
}