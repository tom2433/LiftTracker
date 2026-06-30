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
import com.example.lifttracker.R
import com.example.lifttracker.ui.AppViewModelProvider
import com.example.lifttracker.ui.navigation.NavigationDestination
import com.example.lifttracker.ui.viewModels.AnalyticsViewModel

object AnalyticsDestination : NavigationDestination {
    override val route = "analytics"
    override val titleRes = R.string.analytics_title
}

/**
 * Entry route for Analytics Screen
 */
@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val analyticsUiState by viewModel.analyticsUiState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Analytics Screen needs to be implemented here"
        )
    }
}