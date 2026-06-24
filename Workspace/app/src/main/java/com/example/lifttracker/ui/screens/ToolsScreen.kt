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

object ToolsDestination : NavigationDestination {
    override val route = "tools"
    override val titleRes = R.string.tools_title
}

/**
 * Entry route for Tools Screen
 */
@Composable
fun ToolsScreen(
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
        titleRes = ToolsDestination.titleRes,
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
                text = "Tools Screen needs to be implemented here"
            )
        }
    }
}