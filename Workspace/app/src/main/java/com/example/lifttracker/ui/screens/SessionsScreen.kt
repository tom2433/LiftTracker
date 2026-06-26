package com.example.lifttracker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifttracker.LiftTrackerDrawer
import com.example.lifttracker.ui.navigation.NavigationDestination
import com.example.lifttracker.R
import com.example.lifttracker.ui.AppViewModelProvider
import com.example.lifttracker.ui.viewModels.SessionsViewModel

object SessionsDestination : NavigationDestination {
    override val route = "sessions"
    override val titleRes = R.string.sessions_title
}

/**
 * Entry route for Sessions Screen
 */
@Composable
fun SessionsScreen(
    navigateToRecordSession: () -> Unit,
    navigateToMuscleGroups: () -> Unit,
    navigateToSessions: () -> Unit,
    navigateToCalendar: () -> Unit,
    navigateToAnalytics: () -> Unit,
    navigateToTools: () -> Unit,
    navigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val sessionsUiState by viewModel.sessionsUiState.collectAsState()

    LiftTrackerDrawer(
        titleRes = SessionsDestination.titleRes,
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
                text = "Sessions Screen needs to be implemented here"
            )
        }
    }
}
