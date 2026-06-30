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
import com.example.lifttracker.ui.viewModels.ToolsViewModel

object ToolsDestination : NavigationDestination {
    override val route = "tools"
    override val titleRes = R.string.tools_title
}

/**
 * Entry route for Tools Screen
 */
@Composable
fun ToolsScreen(
    modifier: Modifier = Modifier,
    viewModel: ToolsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val toolsUiState by viewModel.toolsUiState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Tools Screen needs to be implemented here"
        )
    }

}