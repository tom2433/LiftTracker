package com.example.lifttracker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifttracker.R
import com.example.lifttracker.data.Profile
import com.example.lifttracker.ui.AppViewModelProvider
import com.example.lifttracker.ui.navigation.NavigationDestination
import com.example.lifttracker.ui.theme.LiftTrackerTheme
import com.example.lifttracker.ui.utils.ShowElementDeleteDialog
import com.example.lifttracker.ui.utils.ShowElementEntryDialog
import com.example.lifttracker.ui.utils.StatRow
import com.example.lifttracker.ui.utils.ThreeDotMenu
import com.example.lifttracker.ui.utils.WelcomeDialog
import com.example.lifttracker.ui.viewModels.MuscleGroupsUiState
import com.example.lifttracker.ui.viewModels.MuscleGroupsViewModel

object MuscleGroupsDestination : NavigationDestination {
    override val route = "muscleGroups"
    override val titleRes = R.string.muscle_groups_title
}

/**
 * Entry route for Muscle Groups Screen (Home Screen)
 */
@Composable
fun MuscleGroupsScreen(
    modifier: Modifier = Modifier,
    viewModel: MuscleGroupsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val muscleGroupsUiState by viewModel.muscleGroupsUiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current

    var hasPlayedInitialAnimation by rememberSaveable { mutableStateOf(false) }
    val muscleGroupsScrollState = rememberScrollState()

    // shared transition layout to hold the muscle group screen content and the
    // Lift Screen. The LiftSection elements are connected to this lift screen,
    // and the transition animation functionality is hoisted to this screen.
    SharedTransitionLayout {
        AnimatedContent(
            targetState = muscleGroupsUiState.liftScreenId
        ) { liftId ->
            if (liftId == -1) {
                MuscleGroupsScreenContent(
                    viewModel = viewModel,
                    muscleGroupsUiState = muscleGroupsUiState,
                    layoutDirection = layoutDirection,
                    goToLiftScreen = {
                        viewModel.openLiftScreen(it)
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent,
                    animateInitialEntry = !hasPlayedInitialAnimation,
                    scrollState = muscleGroupsScrollState,
                    modifier = modifier
                )

                LaunchedEffect(Unit) {
                    hasPlayedInitialAnimation = true
                }
            } else {
                LiftScreen(
                    liftId = liftId,
                    onBackPressed = {
                        viewModel.dismissLiftScreen()
                    },
                    animatedVisibilityScope = this@AnimatedContent,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
        }
    }
}

@Composable
fun MuscleGroupsScreenContent(
    viewModel: MuscleGroupsViewModel,
    muscleGroupsUiState: MuscleGroupsUiState,
    layoutDirection: LayoutDirection,
    goToLiftScreen: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    animateInitialEntry: Boolean,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    // check if the user has created a profile or not
    // if they haven't, then the welcome dialog will be visible
    if (muscleGroupsUiState.welcomeDialogVisible) {
        // display the welcome message
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(
                        start = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateStartPadding(layoutDirection),
                        end = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateEndPadding(layoutDirection)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // welcome message here, do not allow user to dismiss unless they enter a profile name
                // and click the create profile button
                WelcomeDialog(
                    buttonEnabled = viewModel.isValidProfileName(),
                    newProfileName = muscleGroupsUiState.newProfileName,
                    newProfileNote = muscleGroupsUiState.newProfileNote,
                    onProfileNameValueChanged = { viewModel.updateNewProfileName(it) },
                    onProfileNoteValueChanged = { viewModel.updateNewProfileNote(it) },
                    onCreateProfile = {
                        viewModel.createProfile()
                    }
                )
            }
        }
    } else {
        // if they have an existing profile, display everything for the current active profile
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // display active profile
            val activeProfile: Profile? = viewModel.getActiveProfile()
            Text(
                text = "Displaying Muscle Groups for Profile: ${activeProfile?.name ?: "not loaded yet"}${if (activeProfile?.note?.isNotBlank() ?: false) " (${activeProfile.note})" else ""}",
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // muscle group cards
            for ((muscleGroupId, muscleGroupDetail) in muscleGroupsUiState.muscleGroupList) {
                key(muscleGroupId) {
                    val visibilityState = remember(muscleGroupId) {
                        MutableTransitionState(
                            initialState = !animateInitialEntry
                        ).apply {
                            targetState = true
                        }
                    }
                    val bottomCornerRadius by animateDpAsState(
                        targetValue = if (muscleGroupDetail.cardIsOpen) 0.dp else 16.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    val elevationDp by animateDpAsState(
                        targetValue = if (muscleGroupDetail.cardIsOpen) 4.dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )

                    LaunchedEffect(muscleGroupsUiState.muscleGroupIdToDelete) {
                        visibilityState.targetState = muscleGroupId != muscleGroupsUiState.muscleGroupIdToDelete
                    }

                    AnimatedVisibility(
                        visibleState = visibilityState,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(300)
                        ) + fadeIn(
                            animationSpec = tween(300)
                        ),
                        exit = slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(300)
                        ) + shrinkVertically(
                            shrinkTowards = Alignment.Top,
                            animationSpec = tween(300)
                        ) + fadeOut(
                            animationSpec = tween(300)
                        )
                    ) {
                        Column {
                            // each muscle group has card design
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = bottomCornerRadius,
                                    bottomEnd = bottomCornerRadius
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 168.dp),
                                onClick = { viewModel.muscleGroupCardClicked(muscleGroupId) },
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = elevationDp,
                                    pressedElevation = elevationDp,
                                    focusedElevation = elevationDp,
                                    hoveredElevation = elevationDp,
                                    draggedElevation = elevationDp,
                                    disabledElevation = elevationDp
                                )
                            ) {
                                // column to hold card contents, 16.dp padding
                                Column(
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    // Row to hold muscle group name and three dot menu
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                top = 8.dp,
                                                start = 16.dp,
                                                end = 8.dp,
                                                bottom = 20.dp
                                            )
                                    ) {
                                        Column {
                                            // muscle group name (title)
                                            Text(
                                                text = muscleGroupDetail.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                modifier = if (muscleGroupDetail.note.isNotBlank()) {
                                                    Modifier.padding(top = 4.dp)
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            // muscle group note (if applicable)
                                            if (muscleGroupDetail.note.isNotBlank()) {
                                                Text(
                                                    text = muscleGroupDetail.note,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }

                                        // three dot menu for edit/delete muscle group
                                        ThreeDotMenu(
                                            contentDescRes = R.string.muscle_group_menu,
                                            expanded = muscleGroupDetail.menuIsOpen,
                                            onClickDots = {
                                                viewModel.openThreeDotMenu(muscleGroupId)
                                            },
                                            onClickEdit = {
                                                viewModel.showEditMuscleGroupDialog(muscleGroupId)
                                                viewModel.closeThreeDotMenu(muscleGroupId)
                                            },
                                            onClickDelete = {
                                                viewModel.showDeleteMuscleGroupDialog(muscleGroupId)
                                                viewModel.closeThreeDotMenu(muscleGroupId)
                                            },
                                            onDismissRequest = {
                                                viewModel.closeThreeDotMenu(muscleGroupId)
                                            }
                                        )
                                    }

                                    // muscle group stats
                                    // # of lifts
                                    StatRow(
                                        label = R.string.num_of_lifts_label,
                                        value = muscleGroupDetail.numLifts.toString(),
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp
                                        )
                                    )

                                    // # of days trained (total)
                                    StatRow(
                                        label = R.string.num_of_sessions_trained_label,
                                        value = muscleGroupDetail.numSessions.toString(),
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp
                                        )
                                    )

                                    // avg # of sessions/week
                                    StatRow(
                                        label = R.string.avg_num_sessions_per_week_label,
                                        value = "%.2f".format(muscleGroupDetail.avgNumSessionsPerWeek),
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp
                                        )
                                    )

                                    // avg # of sets/session
                                    StatRow(
                                        label = R.string.avg_num_sets_per_session_label,
                                        value = "%.2f".format(muscleGroupDetail.avgNumSetsPerSession),
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp
                                        )
                                    )

                                    // avg # of sets/week
                                    StatRow(
                                        label = R.string.avg_num_sets_per_week_label,
                                        value = "%.2f".format(muscleGroupDetail.avgNumSetsPerWeek),
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp
                                        )
                                    )

                                    // avg # of reps/set
                                    StatRow(
                                        label = R.string.avg_num_reps_per_set_label,
                                        value = if (muscleGroupDetail.avgNumRepsPerSet != -1.0) {
                                            "%.2f".format(muscleGroupDetail.avgNumRepsPerSet)
                                        } else {
                                            "N/A"
                                        },
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp
                                        )
                                    )

                                    // last date trained (Today, Yesterday, 2 days ago, 3, ..., 6, 1 week ago,
                                    // 2 weeks ago, ..., 1 month ago, Over 1 month ago, 2 months ago, 3, 4, ...,
                                    // 1 year ago, Over 1 year ago, Never)
                                    StatRow(
                                        label = R.string.last_date_trained_label,
                                        value = muscleGroupDetail.lastDateTrained,
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            bottom = 16.dp
                                        )
                                    )
                                }
                            }

                            // animated dropdown for when the user clicks on this card
                            AnimatedVisibility(
                                visible = muscleGroupDetail.cardIsOpen,
                                enter = expandVertically(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + fadeIn(),
                                exit = shrinkVertically(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + fadeOut()
                            ) {
                                Column {
                                    // LiftSection for current muscle group
                                    LiftSection(
                                        muscleGroupId = muscleGroupId,
                                        goToLiftScreen = goToLiftScreen,
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedVisibilityScope = animatedVisibilityScope
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // muscle group edit dialog
            if (muscleGroupsUiState.muscleGroupEditDialogVisible) {
                ShowElementEntryDialog(
                    dialogTitle = stringResource(R.string.edit_muscle_group),
                    submitBtnText = stringResource(R.string.update_muscle_group),
                    elementNameInputLabel = stringResource(R.string.muscle_group_name),
                    elementNoteInputLabel = stringResource(R.string.muscle_group_note),
                    buttonEnabled = viewModel.validateMuscleGroup(),
                    newElementName = muscleGroupsUiState.muscleGroupToEdit?.name ?: "",
                    newElementNote = muscleGroupsUiState.muscleGroupToEdit?.note ?: "",
                    onElementNameValueChanged = {
                        viewModel.updateMuscleGroupName(it)
                    },
                    onElementNoteValueChanged = {
                        viewModel.updateMuscleGroupNote(it)
                    },
                    onSubmit = {
                        viewModel.updateMuscleGroup()
                    },
                    onDismissRequest = {
                        viewModel.dismissEditMuscleGroupDialog()
                    }
                )
            }

            // muscle group delete dialog
            if (muscleGroupsUiState.muscleGroupDeleteDialogVisible) {
                ShowElementDeleteDialog(
                    dialogTitle = "Delete '${muscleGroupsUiState.muscleGroupToDelete?.name ?: "null"}'?",
                    warningDescription = R.string.delete_muscle_group_warning,
                    deleteBtnText = R.string.delete_muscle_group_btn_text,
                    onDismissRequest = {
                        viewModel.dismissDeleteMuscleGroupDialog()
                    },
                    onDelete = {
                        viewModel.deleteMuscleGroup()
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeDialogPreview() {
    LiftTrackerTheme(dynamicColor = false, darkTheme = false) {
        val layoutDirection = LocalLayoutDirection.current

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(
                        start = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateStartPadding(layoutDirection),
                        end = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateEndPadding(layoutDirection)
                    ),
                contentAlignment = Alignment.Center
            ) {
                WelcomeDialog(
                    buttonEnabled = true,
                    newProfileName = "",
                    newProfileNote = "",
                    onProfileNameValueChanged = {},
                    onProfileNoteValueChanged = {},
                    onCreateProfile = {},
                    modifier = Modifier
                )
            }
        }
    }
}
