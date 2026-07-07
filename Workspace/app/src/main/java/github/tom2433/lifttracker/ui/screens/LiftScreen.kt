package github.tom2433.lifttracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Scale
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import github.tom2433.lifttracker.R
import github.tom2433.lifttracker.data.Lift
import github.tom2433.lifttracker.ui.AppViewModelProvider
import github.tom2433.lifttracker.ui.utils.ThreeDotMenu
import github.tom2433.lifttracker.ui.viewModels.LiftScreenViewModel

@Composable
fun LiftScreen(
    lift: Lift,
    onBackPressed: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier,
    viewModel: LiftScreenViewModel = viewModel(
        key = "lift_${lift.id}",
        factory = AppViewModelProvider.liftScreenFactory(lift)
    )
) {
    BackHandler {
        onBackPressed()
    }
    val layoutDirection = LocalLayoutDirection.current
    val liftScreenUiState by viewModel.liftScreenUiState.collectAsState()

    with (sharedTransitionScope) {
        val screenFullyRendered =
            (animatedVisibilityScope.transition.currentState == EnterExitState.Visible &&
            animatedVisibilityScope.transition.targetState == EnterExitState.Visible)

        Box(
            modifier = modifier
                .fillMaxSize()
                .sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = lift.id.toString()
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
                        .padding(
                            top = 16.dp,
                            bottom = 16.dp,
                            start = 24.dp,
                            end = 24.dp
                        )
                ) {
                    // box to hold collapse button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clickable(
                                onClick = { onBackPressed() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        this@Column.AnimatedVisibility(
                            visible = screenFullyRendered,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(150)
                            ) + fadeIn(tween(150)),
                            exit = slideOutVertically(
                                targetOffsetY = { -it },
                                animationSpec = tween(150)
                            ) + fadeOut(tween(150))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = stringResource(R.string.close_lift_screen),
                                modifier = Modifier.padding(
                                    vertical = 2.dp
                                )
                            )
                        }
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
                                text = liftScreenUiState.lift.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(
                                        key = "${lift.id}-name"
                                    ),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            )
                            // lift note (if applicable)
                            if (liftScreenUiState.lift.note.isNotBlank()) {
                                Text(
                                    text = liftScreenUiState.lift.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.sharedElement(
                                        sharedContentState = rememberSharedContentState(
                                            key = "${lift.id}-note"
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
                                    key = "${lift.id}-menu"
                                ),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )
                    }

                    // animated visibility for the rest of the content to animate in/out
                    AnimatedVisibility(
                        visible = screenFullyRendered,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(150)
                        ) + fadeIn(tween(150)),
                        exit = slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(150)
                        ) + fadeOut(tween(150))
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start
                        ) {
                            // divider to separate
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = 8.dp,
                                        bottom = 16.dp
                                    )
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.Center,
                                verticalArrangement = Arrangement.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // muscle group
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_arm_flex),
                                        contentDescription = stringResource(R.string.muscle_group)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = liftScreenUiState.muscleGroup?.name ?: "null",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.width(28.dp))

                                // metric type
                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_ruler),
                                        contentDescription = stringResource(R.string.metric_type),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = liftScreenUiState.liftScreenDetail.metricType,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.width(28.dp))

                                // units
                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Scale,
                                        contentDescription = stringResource(R.string.unit)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = liftScreenUiState.liftScreenDetail.unitName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

//                            // rows for muscle group, metric type, and unit type
//                            // Row for muscle group
//                            IconStatRow(
//                                painter = painterResource(R.drawable.ic_arm_flex),
//                                contentDescription = stringResource(R.string.muscle_group),
//                                value = liftScreenUiState.muscleGroup?.name ?: "null",
//                                modifier = Modifier.padding(bottom = 8.dp)
//                            )
//
//                            // row for metric type
//                            IconStatRow(
//                                painter = painterResource(R.drawable.ic_ruler),
//                                contentDescription = stringResource(R.string.metric_type),
//                                value = liftScreenUiState.liftScreenDetail.metricType
//                            )
//
//                            // row for units
//                            IconStatRow(
//                                imageVector = Icons.Filled.Scale,
//                                contentDescription = stringResource(R.string.unit),
//                                value = liftScreenUiState.liftScreenDetail.unitName
//                            )
                        }
                    }
                }
            }
        }
    }
}