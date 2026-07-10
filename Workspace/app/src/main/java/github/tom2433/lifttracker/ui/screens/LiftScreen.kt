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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
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
import github.tom2433.lifttracker.data.lift.Lift
import github.tom2433.lifttracker.ui.AppViewModelProvider
import github.tom2433.lifttracker.ui.utils.CustomFilterChip
import github.tom2433.lifttracker.ui.utils.InfoButton
import github.tom2433.lifttracker.ui.utils.SectionTitle
import github.tom2433.lifttracker.ui.utils.ShowLiftEntryDialog
import github.tom2433.lifttracker.ui.utils.ThreeDotMenu
import github.tom2433.lifttracker.ui.utils.ShowMuscleGroupSelectionDialog
import github.tom2433.lifttracker.ui.utils.StatRow
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        // Column to hold name and note if applicable
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
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
                                viewModel.openLiftEditDialog()
                                viewModel.closeThreeDotMenu()
                            },
                            onClickElement2 = {
                                viewModel.openSwitchMuscleGroupDialog()
                                viewModel.closeThreeDotMenu()
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

                                // lift unit
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

                            // last date trained
                            StatRow(
                                label = stringResource(R.string.last_date_trained_label),
                                value = liftScreenUiState.lastDateTrained,
                                modifier = Modifier.padding(
                                    top = 20.dp,
                                    bottom = 20.dp
                                )
                            )

                            // Stats section title
                            SectionTitle {
                                Icon(
                                    imageVector = Icons.Filled.BarChart,
                                    contentDescription = stringResource(R.string.lift_stats)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {
                                // filter chips
                                FlowRow(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalArrangement = Arrangement.Top,
                                    modifier = Modifier
                                ) {
                                    for ((chipLabel, selected) in liftScreenUiState.statDisplayFilterMap) {
                                        CustomFilterChip(
                                            label = chipLabel,
                                            onClick = { viewModel.filterChipClicked(chipLabel) },
                                            selected = selected,
                                            modifier = Modifier
                                                .padding(
                                                    end = 8.dp
                                                )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // info button explaining stat rows below
                                InfoButton {
                                    // total # of sets performed
                                    Text(
                                        text = "Total # of sets performed",
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                    Text(
                                        text = "The total number of sets you've performed for this lift within the selected time period.",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                vertical = 4.dp
                                            )
                                    )

                                    // Avg. # of sets per session
                                    Text(
                                        text = "Avg. # of sets per session",
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                    Text(
                                        text = "The average number of sets completed for this lift per session within the selected time period. Only considers sessions in which this lift was trained.",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                vertical = 4.dp
                                            )
                                    )

                                    // % of overall set volume
                                    Text(
                                        text = "% of overall set volume",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        text = "The proportion of total sets completed for this lift to the number of sets completed for all lifts for all muscle groups within the selected time period as a percent.",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                vertical = 4.dp
                                            )
                                    )

                                    // % of set volume for {muscle group}
                                    Text(
                                        text = "% of set volume for ${liftScreenUiState.muscleGroup?.name ?: "null"}",
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                    Text(
                                        text = "The proportion of total sets completed for this lift to the number of sets completed for all lifts for only this muscle group within the selected time period as a percent.",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                vertical = 4.dp
                                            )
                                    )

                                    // Avg. weight
                                    Text(
                                        text = "Avg. weight",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        text = "The average weight recorded for this lift during the selected time period.",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                vertical = 4.dp
                                            )
                                    )

                                    if (liftScreenUiState.lift.metric_type == 1) {
                                        // Avg. # of reps per set
                                        Text(
                                            text = "Avg. # of reps per set",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            text = "The average number of reps completed per set of this lift during the selected time period.",
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    } else {
                                        // Avg. time per set
                                        Text(
                                            text = "Avg. time per set",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            text = "The average amount of time recorded per set of this lift during the selected time period.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            // stat rows applying to above filter
                            for ((chipLabel, selected) in liftScreenUiState.statDisplayFilterMap) {
                                if (selected) {
                                    for ((label, value) in (when (chipLabel) {
                                        "Past Month" -> liftScreenUiState.pastMonthStatMap
                                        "Past Year" -> liftScreenUiState.pastYearStatMap
                                        "LifeTime" -> liftScreenUiState.lifetimeStatMap
                                        else -> liftScreenUiState.pastMonthStatMap
                                    })) {
                                        StatRow(
                                            label = label,
                                            value = value
                                        )
                                    }
                                }
                            }

                            SectionTitle(
                                modifier = Modifier.padding(top = 40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = stringResource(R.string.lift_history)
                                )
                            }

                            Text(text = "Implement most recent lift sets here")
                        }
                    }
                }
            }
        }
    }

    if (liftScreenUiState.userIsEditingLift) {
        // show edit lift dialog
        ShowLiftEntryDialog(
            dialogTitle = "Edit '${liftScreenUiState.lift.name}' in ${liftScreenUiState.muscleGroup?.name ?: "null"}",
            submitBtnText = stringResource(R.string.update_lift),
            buttonEnabled = viewModel.validateLift(),
            newLiftName = liftScreenUiState.newLiftName,
            newLiftNote = liftScreenUiState.newLiftNote,
            newLiftUnitName = liftScreenUiState.newLiftUnitName,
            onLiftNameValueChanged = { viewModel.updateLiftName(it) },
            onLiftNoteValueChanged = { viewModel.updateLiftNote(it) },
            repsSelected = liftScreenUiState.newLiftMetricType == 1,
            onRepsSelected = { viewModel.selectReps() },
            timeSelected = liftScreenUiState.newLiftMetricType == 2,
            onTimeSelected = { viewModel.selectTime() },
            liftUnitList = liftScreenUiState.liftUnitList,
            onLiftUnitValueChanged = { viewModel.updateLiftUnit(it) },
            onSubmit = { viewModel.updateLift() },
            onDismissRequest = { viewModel.closeLiftEditDialog() }
        )
    }

    if (liftScreenUiState.userIsSwitchingMuscleGroup) {
        ShowMuscleGroupSelectionDialog(
            dialogTitle = "Move ${liftScreenUiState.lift.name} To Another Muscle Group",
            onDismissRequest = { viewModel.closeSwitchMuscleGroupDialog() },
            muscleGroupList = liftScreenUiState.muscleGroups,
            onMuscleGroupSelected = {
                viewModel.selectMuscleGroup(
                    muscleGroupToSelect = it
                )
            },
            selectedMuscleGroup = liftScreenUiState.selectedMuscleGroup,
            onSubmit = { viewModel.submitSwitchMuscleGroupDialog() },
            buttonEnabled = viewModel.validateSwitchMuscleGroupDialog(),
            submitBtnText = "Move ${liftScreenUiState.lift.name} to ${liftScreenUiState.selectedMuscleGroup?.name ?: "null"}",
        )
    }
}
