package github.tom2433.lifttracker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
                        onClick = { viewModel.beginSession() }
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
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // row at top to show play icon at left and finish lift at the right
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    // play icon
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Session in progress",
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(
                                    key = "icon"
                                ),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .padding(end = 8.dp)
                    )

                    // finish session button
                    OutlinedButton(
                        onClick = { viewModel.finishSession() }
                    ) {
                        Text("Finish")
                    }
                }

                // divider to separate scrollable content
                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                // implement scrollable content here
            }
        }
    }
}