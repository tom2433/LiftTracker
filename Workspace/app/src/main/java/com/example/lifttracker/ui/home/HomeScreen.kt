package com.example.lifttracker.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.lifttracker.LiftTrackerDrawer
import com.example.lifttracker.R
import com.example.lifttracker.ui.navigation.NavigationDestination

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

/**
 * Entry route for Home Screen
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    LiftTrackerDrawer {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Welcome to Lift Tracker"
            )
        }
    }
}