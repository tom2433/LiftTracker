package github.tom2433.lifttracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import github.tom2433.lifttracker.R
import github.tom2433.lifttracker.ui.AppViewModelProvider
import github.tom2433.lifttracker.ui.utils.ThreeDotMenu
import github.tom2433.lifttracker.ui.viewModels.LiftScreenViewModel

@Composable
fun LiftScreen(
    liftId: Int,
    onBackPressed: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier,
    viewModel: LiftScreenViewModel = viewModel(
        key = "lift_$liftId",
        factory = AppViewModelProvider.liftScreenFactory(liftId)
    )
) {
    BackHandler {
        onBackPressed()
    }
    val layoutDirection = LocalLayoutDirection.current
    val liftScreenUiState by viewModel.liftScreenUiState.collectAsState()

    with (sharedTransitionScope) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = liftId.toString()
                    ),
                    animatedVisibilityScope = animatedVisibilityScope
                )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateStartPadding(layoutDirection),
                        end = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateEndPadding(layoutDirection)
                    ),
                shape = RoundedCornerShape(16.dp)
            ) {
                // Column to hold card contents
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // box to hold collapse button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = { onBackPressed() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = stringResource(R.string.close_lift_screen),
                            modifier = Modifier.padding(
                                vertical = 2.dp
                            )
                        )
                    }

                    // Top region to hold lift name, note, and three dot menu
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        // Column to hold name and note if applicable
                        Column {
                            // lift name
                            Text(
                                text = liftScreenUiState.lift?.name ?: "null",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(
                                        key = "$liftId-name"
                                    ),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            )
                            // lift note (if applicable)
                            if (liftScreenUiState.lift?.note?.isNotBlank() ?: false) {
                                Text(
                                    text = liftScreenUiState.lift?.note ?: "null",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.sharedElement(
                                        sharedContentState = rememberSharedContentState(
                                            key = "$liftId-note"
                                        ),
                                        animatedVisibilityScope = animatedVisibilityScope
                                    )
                                )
                            }
                        }

                        // spacer to separate name and note from three dot menu
                        Spacer(modifier = Modifier.weight(1f))

                        // three dot menu for edit and move to different muscle group
                        ThreeDotMenu(
                            contentDescRes = R.string.lift_detail_menu,
                            element1TextRes = R.string.edit,
                            element2TextRes = R.string.move_to_another_muscle_group,
                            expanded = liftScreenUiState.threeDotMenuOpen,
                            onClickDots = {
                                viewModel.openThreeDotMenu()
                            },
                            onClickElement1 = {
                                viewModel.closeThreeDotMenu() // TODO
                            },
                            onClickElement2 = {
                                viewModel.closeThreeDotMenu() // TODO
                            },
                            onDismissRequest = {
                                viewModel.closeThreeDotMenu()
                            },
                            modifier = Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(
                                    key = "$liftId-menu"
                                ),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )
                    }

                    // divider to separate
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}