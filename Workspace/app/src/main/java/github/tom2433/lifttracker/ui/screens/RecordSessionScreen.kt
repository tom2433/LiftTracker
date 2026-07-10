package github.tom2433.lifttracker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import github.tom2433.lifttracker.R
import github.tom2433.lifttracker.ui.AppViewModelProvider
import github.tom2433.lifttracker.ui.navigation.NavigationDestination
import github.tom2433.lifttracker.ui.viewModels.RecordSessionUiState
import github.tom2433.lifttracker.ui.viewModels.RecordSessionViewModel

object RecordSessionDestination : NavigationDestination {
    override val route = "recordSession"
    override val titleRes = R.string.record_session_title
}

/**
 * Entry route for Record session screen
 */
@Composable
fun RecordSessionScreen(
    modifier: Modifier = Modifier,
    viewModel: RecordSessionViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val recordSessionUiState by viewModel.recordSessionUiState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // animated transition between big play button and in progress screen
        SharedTransitionLayout {
            AnimatedContent(
                targetState = recordSessionUiState.activeLiftDay
            ) { liftDay ->
                // if no lift day in progress, display button to begin the lift day
                if (liftDay == null) {
                    Card(
                        shape = CircleShape,
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(
                                    key = "container"
                                ),
                                animatedVisibilityScope = this@AnimatedContent
                            )
                            .size(100.dp),
                        colors = CardDefaults.cardColors().copy(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = {}
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Begin Session",
                                modifier = Modifier
                                    .sharedElement(
                                        sharedContentState = rememberSharedContentState(
                                            key = "icon"
                                        ),
                                        animatedVisibilityScope = this@AnimatedContent
                                    )
                                    .size(40.dp)
                            )
                        }
                    }
                } else {
                    SessionInProgressScreen(
                        viewModel = viewModel,
                        recordSessionUiState = recordSessionUiState,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent
                    )
                }
            }
        }
    }
}

@Composable
fun SessionInProgressScreen(
    viewModel: RecordSessionViewModel,
    recordSessionUiState: RecordSessionUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = "container"
                    ),
                    animatedVisibilityScope = animatedVisibilityScope
                )
        ) {
            // add top row with play button icon and a finish lift button
        }
    }
}