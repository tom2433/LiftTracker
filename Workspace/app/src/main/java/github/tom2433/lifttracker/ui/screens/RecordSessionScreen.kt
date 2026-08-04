package github.tom2433.lifttracker.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import github.tom2433.lifttracker.R
import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.session.Session
import github.tom2433.lifttracker.data.setmetric.SetMetric
import github.tom2433.lifttracker.data.structures.LiftSearchDetail
import github.tom2433.lifttracker.data.structures.SetMetricDisplayDetail
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
import github.tom2433.lifttracker.ui.AppViewModelProvider
import github.tom2433.lifttracker.ui.navigation.NavigationDestination
import github.tom2433.lifttracker.ui.utils.DisplaySetCountPerMuscleGroup
import github.tom2433.lifttracker.ui.utils.LiftDetailFlowRow
import github.tom2433.lifttracker.ui.utils.LiftSetLabels
import github.tom2433.lifttracker.ui.utils.SetNumberRow
import github.tom2433.lifttracker.ui.utils.ShowElementDeleteDialog
import github.tom2433.lifttracker.ui.utils.ShowElementEntryDialog
import github.tom2433.lifttracker.ui.utils.ShowMetricEntryDialog
import github.tom2433.lifttracker.ui.viewModels.RecordSessionUiState
import github.tom2433.lifttracker.ui.viewModels.RecordSessionViewModel
import kotlinx.coroutines.delay

object RecordSessionDestination : NavigationDestination {
    override val route = "recordSession"
    override val titleRes = R.string.record_session_title
}

/**
 * Entry route for Record session screen
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordSessionScreen(
    modifier: Modifier = Modifier,
    viewModel: RecordSessionViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val recordSessionUiState by viewModel.recordSessionUiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // animated transition between big play button and in progress screen
        SharedTransitionLayout {
            AnimatedContent(
                targetState = recordSessionUiState.activeSession
            ) { activeSession ->
                // if no session is in progress, display button to begin a session
                if (activeSession == null) {
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
                        animatedVisibilityScope = this@AnimatedContent,
                        activeSession = activeSession
                    )
                }
            }
        }
    }

    // dialog to delete a lift in progress
    if (recordSessionUiState.deleteLiftInProgressDialogVisible &&
        recordSessionUiState.liftIdToDelete != -1) {
        val title: String = recordSessionUiState.liftDetailMap[recordSessionUiState.liftIdToDelete]?.liftObj?.name ?: "null"

        ShowElementDeleteDialog(
            dialogTitle = "Delete all sets completed in this session for ${title}?",
            warningDescription = stringResource(R.string.delete_lift_in_progress_warning_desc),
            deleteBtnText = "Delete this session's sets for $title",
            onDismissRequest = { viewModel.dismissDeleteLiftInProgressDialog() },
            onDelete = { viewModel.deleteLiftInProgress() }
        )
    }
    
    // dialog to edit a lift set
    if (recordSessionUiState.editLiftSetDialogVisible) {
        ShowElementEntryDialog(
            dialogTitle = "Edit ${recordSessionUiState.liftSetToEdit?.set_label ?: "null"} of ${recordSessionUiState.liftWithSetToEdit?.name ?: "null"}",
            submitBtnText = "Update ${recordSessionUiState.liftSetToEdit?.set_label ?: "null"}",
            elementNameInputLabel = "Set Label",
            elementNoteInputLabel = "Set Note (optional)",
            buttonEnabled = viewModel.validateLiftSetEntry(),
            newElementName = recordSessionUiState.newLiftSetLabel,
            newElementNote = recordSessionUiState.newLiftSetNote,
            onElementNameValueChanged = { viewModel.updateLiftSetName(it) },
            onElementNoteValueChanged = { viewModel.updateLiftSetNote(it) },
            onSubmit = { viewModel.updateLiftSet() },
            onDismissRequest = { viewModel.dismissEditLiftSetDialog() }
        )
    }

    // dialog to edit a SetMetric's note
    if (recordSessionUiState.editSetMetricNoteDialogVisible &&
        recordSessionUiState.setMetricToEdit != null &&
        recordSessionUiState.liftWithSetMetricToEdit != null) {
        val unitName: String = recordSessionUiState.liftDetailMap[recordSessionUiState.liftWithSetMetricToEdit?.id]?.unitName ?: "null"
        val setLabel: String = recordSessionUiState.liftSetWithSetMetricToEdit?.set_label ?: "null"
        val liftName: String = recordSessionUiState.liftWithSetMetricToEdit?.name ?: "null"
        val metricType: String = recordSessionUiState.liftDetailMap[recordSessionUiState.liftWithSetMetricToEdit?.id]?.metricType ?: "null"
        val setMetricIsWeight: Boolean = ((recordSessionUiState.setMetricToEdit?.metric_position ?: 1) == 1)

        ShowMetricEntryDialog(
            dialogTitle = if (setMetricIsWeight) {
                // SetMetric is a weight metric with units
                "Edit note for $unitName of $setLabel for $liftName"
            } else {
                // SetMetric is a rep or time value
                "Edit note for $metricType of $setLabel for $liftName"
            },
            submitBtnText = "Update note for $setLabel",
            metricInputLabel = stringResource(R.string.note),
            buttonEnabled = true,
            newMetric = recordSessionUiState.newSetMetricNote,
            onMetricValueChanged = { viewModel.updateSetMetricNote(it) },
            onSubmit = { viewModel.updateSetMetric() },
            onDismissRequest = { viewModel.dismissEditSetMetricNoteDialog() }
        )
    }

    // dialog to delete a lift set in progress
    if (recordSessionUiState.deleteLiftSetDialogVisible) {
        val setLabel = recordSessionUiState.liftSetToDelete?.set_label ?: "null"
        var liftSetLiftId: Int? = null
        for ((liftId, nestedMap) in recordSessionUiState.liftSetMap) {
            for (liftSet in nestedMap.keys) {
                if (liftSet.id == recordSessionUiState.liftSetToDelete?.id) {
                    liftSetLiftId = liftId
                    break
                }
            }
            if (liftSetLiftId != null) break
        }
        val liftName: String = recordSessionUiState.liftDetailMap[liftSetLiftId]?.liftObj?.name ?: "null"

        ShowElementDeleteDialog(
            dialogTitle = "Delete this session's $setLabel of ${liftName}?",
            warningDescription = stringResource(R.string.point_of_no_return),
            deleteBtnText = "Delete $setLabel",
            onDismissRequest = { viewModel.dismissDeleteLiftSetDialog() },
            onDelete = { viewModel.deleteLiftSet() }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionInProgressScreen(
    viewModel: RecordSessionViewModel,
    recordSessionUiState: RecordSessionUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    activeSession: Session,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    with(sharedTransitionScope) {
        // determine when screen is fully rendered so that enter/exit animations
        // are triggered appropriately
        val screenFullyRendered =
            (animatedVisibilityScope.transition.currentState == EnterExitState.Visible &&
            animatedVisibilityScope.transition.targetState == EnterExitState.Visible)

        val screenContentColor = MaterialTheme.colorScheme.onSecondaryContainer

        // card holding the entire in progress screen
        Card(
            modifier = modifier
                .fillMaxSize()
                .sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = "container"
                    ),
                    animatedVisibilityScope = animatedVisibilityScope
                )
        ) {
            // column to hold card contents
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
                    // row to hold play icon and name/date/note for current session
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                onClick = {
                                    viewModel.showSessionEditDialog()
                                }
                            )
                    ) {
                        // play icon
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_session_name_and_note),
                            modifier = Modifier
                                .sharedElement(
                                    sharedContentState = rememberSharedContentState(
                                        key = "icon"
                                    ),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                                .padding(
                                    end = 12.dp,
                                    top = 4.dp,
                                    bottom = 4.dp
                                )
                        )

                        // column to hold session name/date/note
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            // session name
                            Text(
                                text = activeSession.session_label,
                                style = MaterialTheme.typography.titleMedium
                            )

                            // session date
                            Text(
                                text = DateTimeCalculator.convertIsoDateToReadableFormat(
                                    isoDate = activeSession.date
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 13.sp,
                                color = CardDefaults.cardColors().contentColor.copy(alpha = 0.75f)
                            )

                            // session note (if applicable)
                            if (activeSession.note.isNotBlank()) {
                                Text(
                                    text = activeSession.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 13.sp,
                                    color = CardDefaults.cardColors().contentColor.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    // finish session button
                    Button(
                        onClick = { viewModel.saveSession() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Finish")
                    }
                }

                // animate the entrances of the horizontal divider and the scrollable content
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
                    // divider to separate scrollable content
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    // scrollable content; holds everything after the top bar
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // indicate how many sets have been performed for each muscle group
                        if (recordSessionUiState.setCountPerMuscleGroupList.isNotEmpty()) {
                            DisplaySetCountPerMuscleGroup(
                                setCountPerMuscleGroupList = recordSessionUiState.setCountPerMuscleGroupList,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // lifts in progress will go here
                        loop@ for ((liftId, setMap) in recordSessionUiState.liftSetMap) {
                            key(liftId) {
                                // check that this liftId is also in the liftDetailMap (it should be)
                                val liftDetail: LiftSearchDetail =
                                    recordSessionUiState.liftDetailMap[liftId] ?: continue@loop

                                // animate the visibility of the entire lift in progress card
                                AnimatedVisibility(
                                    visible = liftDetail.visible,
                                    enter = slideInHorizontally(
                                        initialOffsetX = { it },
                                        animationSpec = tween(150)
                                    ) + fadeIn(tween(150)),
                                    exit = slideOutHorizontally(
                                        targetOffsetX = { -it },
                                        animationSpec = tween(150)
                                    ) + fadeOut(tween(150))
                                ) {
                                    // card to represent lift in progress
                                    LiftInProgressCard(
                                        toggleSelectedCard = { viewModel.toggleSelectedInProgressLiftCard(liftId) },
                                        onLiftDelete = { viewModel.showDeleteLiftInProgressDialog(liftId) },
                                        showEditLiftSetDialog = {
                                            viewModel.showEditLiftSetDialog(
                                                liftSet = it,
                                                lift = recordSessionUiState.liftDetailMap[liftId]?.liftObj ?: return@LiftInProgressCard
                                            )
                                        },
                                        showEditSetMetricNoteDialog = { setMetric, liftSet ->
                                            viewModel.showEditSetMetricNoteDialog(
                                                setMetricToEdit = setMetric,
                                                liftSetWithSetMetricToEdit = liftSet,
                                                liftWithSetMetricToEdit = liftDetail.liftObj
                                            )
                                        },
                                        showDeleteLiftSetDialog = { viewModel.showDeleteLiftSetDialog(it) },
                                        addLiftSet = { viewModel.addLiftSetForLiftId(liftId) },
                                        onSetMetricValueChanged = { newValue, setMetric ->
                                            viewModel.setMetricValueChanged(newValue, setMetric)
                                        },
                                        onSetMetricTimeValueChanged = { newValue: String, setMetric: SetMetric, inputType: String ->
                                            viewModel.setMetricTimeValueChanged(newValue, setMetric, inputType)
                                        },
                                        liftDetail = liftDetail,
                                        setMap = setMap,
                                        setMetricDisplayDetailMap = recordSessionUiState.setMetricDisplayDetailMap,
                                        liftSetVisibleMap = recordSessionUiState.liftSetVisibleMap,
                                        setCountPerLiftMap = recordSessionUiState.setCountPerLiftMap,
                                        focusManager = focusManager,
                                        deleteButtonsEnabled = recordSessionUiState.deleteButtonsEnabled,
                                        activeSession = activeSession
                                    )
                                }
                            }
                        }

                        // last lift card will be a prompt if the user is adding a lift
                        AnimatedVisibility(
                            visible = recordSessionUiState.userIsAddingLift,
                            enter = slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(150)
                            ) + fadeIn(tween(150)),
                            exit = slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(150)
                            ) + fadeOut(tween(150))
                        ) {
                            ExistingLiftEntryCard(
                                liftSuggestionsList = recordSessionUiState.liftSuggestionsList,
                                inputLiftName = recordSessionUiState.inputLiftName,
                                onInputLiftValueChanged = { viewModel.onInputLiftValueChanged(it) },
                                onClickCancel = { viewModel.cancelAddLift() },
                                dismissDropdownSuggestionList = { viewModel.dismissDropdownSuggestionList() },
                                liftSuggestionClicked = { viewModel.liftSuggestionClicked(it) },
                                dropdownButtonClicked = { viewModel.liftEntryDropdownButtonClicked() },
                                onGo = { viewModel.onGoLiftEntry() },
                                screenContentColor = screenContentColor,
                                deleteButtonsEnabled = recordSessionUiState.deleteButtonsEnabled
                            )
                        }

                        // add button to add a lift
                        HorizontalAddButtonDivider(
                            onClickAdd = { viewModel.addLiftClicked() },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (recordSessionUiState.sessionEditDialogVisible) {
        ShowElementEntryDialog(
            dialogTitle = stringResource(R.string.edit_session_dialog_title),
            submitBtnText = stringResource(R.string.update_session),
            elementNameInputLabel = stringResource(R.string.session_name),
            elementNoteInputLabel = stringResource(R.string.session_note_optional),
            buttonEnabled = viewModel.validateSessionInput(),
            newElementName = recordSessionUiState.newSessionName,
            newElementNote = recordSessionUiState.newSessionNote,
            onElementNameValueChanged = { viewModel.updateNewSessionName(it) },
            onElementNoteValueChanged = { viewModel.updateNewSessionNote(it) },
            onSubmit = { viewModel.updateSessionNameAndNote() },
            onDismissRequest = { viewModel.dismissSessionEditDialog() }
        )
    }
}

@Composable
fun LiftInProgressCard(
    toggleSelectedCard: () -> Unit,
    onLiftDelete: () -> Unit,
    showEditLiftSetDialog: (LiftSet) -> Unit,
    showEditSetMetricNoteDialog: (SetMetric, LiftSet) -> Unit,
    showDeleteLiftSetDialog: (LiftSet) -> Unit,
    addLiftSet: () -> Unit,
    onSetMetricValueChanged: (String, SetMetric) -> Unit,
    onSetMetricTimeValueChanged: (String, SetMetric, String) -> Unit,
    liftDetail: LiftSearchDetail,
    setMap: Map<LiftSet, Pair<SetMetric, SetMetric>>,
    setMetricDisplayDetailMap: Map<Int, SetMetricDisplayDetail>,
    liftSetVisibleMap: Map<Int, Boolean>,
    setCountPerLiftMap: Map<Int, Int>,
    focusManager: FocusManager,
    deleteButtonsEnabled: Boolean,
    activeSession: Session,
    modifier: Modifier = Modifier
) {
    val bottomCornerRadius by animateDpAsState(
        targetValue = if (liftDetail.selected) 0.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )
    val screenContentColor = MaterialTheme.colorScheme.onSecondaryContainer

    Column(
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        // card to represent lift in progress
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = bottomCornerRadius,
                bottomEnd = bottomCornerRadius
            ),
            colors = CardDefaults.cardColors().copy(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier
                .fillMaxWidth(),
            onClick = { toggleSelectedCard() }
        ) {
            // column to hold card contents. content should be minimal
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
            ) {
                // row to hold lift name and note on left, delete button on right
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // column to hold lift name and note if applicable, and set count
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // lift name
                        Text(
                            text = liftDetail.liftObj.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        // lift note (if applicable)
                        if (liftDetail.liftObj.note.isNotBlank()) {
                            Text(
                                text = liftDetail.liftObj.note,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                    alpha = 0.75f
                                )
                            )
                        }
                        // lift set count
                        Text(
                            text = if (setCountPerLiftMap[liftDetail.liftObj.id] == 1) {
                                "1 set"
                            } else {
                                "${setCountPerLiftMap[liftDetail.liftObj.id]} sets"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                alpha = 0.75f
                            )
                        )
                    }

                    // icon button to delete
                    IconButton(
                        onClick = onLiftDelete,
                        enabled = deleteButtonsEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete_lift_in_progress),
                        )
                    }
                }

                // animate the visibility of the lift detail flow row
                AnimatedVisibility(
                    visible = liftDetail.selected,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeOut(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                ) {
                    LiftDetailFlowRow(
                        muscleGroupName = liftDetail.muscleGroupName,
                        metricType = liftDetail.metricType,
                        unitName = liftDetail.unitName,
                        modifier = Modifier.padding(top = 24.dp),
                        tintColor = screenContentColor.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // animated visibility for lift sets and add lift set button outside the lift IP card
        AnimatedVisibility(
            visible = liftDetail.selected,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        ) {
            // column to hold lift set cards and add lift set button
            Column {
                // column to hold lift set cards
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp
                    )
                ) {
                    // lift sets go here
                    for ((liftSet, setMetricPair) in setMap) {
                        // key to differentiate lift set cards
                        key(liftSet.id) {
                            // animate the entry/exit of each lift set card
                            AnimatedVisibility(
                                visible = liftSetVisibleMap[liftSet.id] ?: false,
                                enter = if (liftSet.lift_set_number != 1) {
                                    slideInHorizontally(
                                        initialOffsetX = { it },
                                        animationSpec = tween(150)
                                    ) + fadeIn(tween(150))
                                } else {
                                    EnterTransition.None
                                },
                                exit = slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(150)
                                ) + fadeOut(tween(150))
                            ) {
                                // card to represent LiftSet. Each LiftSet has two SetMetrics
                                LiftSetInProgressCard(
                                    liftSet = liftSet,
                                    showEditLiftSetDialog = showEditLiftSetDialog,
                                    showEditSetMetricNoteDialog = showEditSetMetricNoteDialog,
                                    showDeleteLiftSetDialog = showDeleteLiftSetDialog,
                                    onSetMetricValueChanged = onSetMetricValueChanged,
                                    onSetMetricTimeValueChanged = onSetMetricTimeValueChanged,
                                    setMetricPair = setMetricPair,
                                    setMetricDisplayDetailMap = setMetricDisplayDetailMap,
                                    liftDetail = liftDetail,
                                    screenContentColor = screenContentColor,
                                    liftHasMoreThanOneSet = setMap.keys.size > 1,
                                    focusManager = focusManager,
                                    deleteButtonsEnabled = deleteButtonsEnabled,
                                    activeSession = activeSession
                                )
                            }
                        }
                    }
                }

                // add lift set button
                Card(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    onClick = { addLiftSet() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.add_lift_set)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun LiftSetInProgressCard(
    liftSet: LiftSet,
    showEditLiftSetDialog: (LiftSet) -> Unit,
    showEditSetMetricNoteDialog: (SetMetric, LiftSet) -> Unit,
    showDeleteLiftSetDialog: (LiftSet) -> Unit,
    onSetMetricValueChanged: (String, SetMetric) -> Unit,
    onSetMetricTimeValueChanged: (String, SetMetric, String) -> Unit,
    setMetricPair: Pair<SetMetric, SetMetric>,
    setMetricDisplayDetailMap: Map<Int, SetMetricDisplayDetail>,
    liftDetail: LiftSearchDetail,
    screenContentColor: Color,
    liftHasMoreThanOneSet: Boolean,
    focusManager: FocusManager,
    deleteButtonsEnabled: Boolean,
    activeSession: Session,
    modifier: Modifier = Modifier
) {
    // focus requesters for each input except weight; user will enter the weight field if they want
    val repFocusRequester = remember { FocusRequester() }
    val hourFocusRequester = remember { FocusRequester() }
    val minuteFocusRequester = remember { FocusRequester() }
    val secondFocusRequester = remember { FocusRequester() }
    // animate the color state of the set metric input fields
    val firstMetricInputColor by animateColorAsState(
        targetValue = if (setMetricDisplayDetailMap[setMetricPair.first.id]?.inputIsLogged ?: false) {
            screenContentColor
        } else {
            MaterialTheme.colorScheme.tertiary
        }
    )
    val secondMetricInputColor by animateColorAsState(
        targetValue = if (setMetricDisplayDetailMap[setMetricPair.second.id]?.inputIsLogged ?: false) {
            screenContentColor
        } else {
            MaterialTheme.colorScheme.tertiary
        }
    )

    // card to represent LiftSet. Each LiftSet has two SetMetrics
    Card(
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        // row to hold lift set details on the left,
        // set metric details on the right
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    top = 16.dp,
                    bottom = 16.dp
                )
                .fillMaxWidth()
        ) {
            // row to hold pencil edit icon and delete icon, then set label/note/set #'s
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.1f)
            ) {
                // column to hold edit icon button and delete icon button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxHeight()
                ) {
                    // pencil icon button to edit LiftSet
                    IconButton(
                        onClick = { showEditLiftSetDialog(liftSet) },
                        enabled = deleteButtonsEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_lift_set)
                        )
                    }

                    // delete icon button to delete lift set
                    // only if this lift has more than one lift set
                    if (liftHasMoreThanOneSet) {
                        IconButton(
                            onClick = { showDeleteLiftSetDialog(liftSet) },
                            enabled = deleteButtonsEnabled
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete_lift_set)
                            )
                        }
                    }
                }

                // column to hold set label, set note (if applicable),
                // and session set # and muscle group session set #
                LiftSetLabels(
                    setLabel = liftSet.set_label,
                    setNote = liftSet.set_note,
                    muscleGroupName = liftDetail.muscleGroupName,
                    sessionSetNumber = liftSet.session_set_number,
                    muscleGroupSessionSetNumber = liftSet.muscle_group_session_set_number,
                    noteColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                        alpha = 0.75f
                    ),
                    borderColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                        alpha = 0.5f
                    ),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            // column to hold user inputs for weight and reps/time
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1.4f)
            ) {
                // row to hold add note icon button and weight text field
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // icon button to add a note for this setMetric
                    IconButton(
                        onClick = {
                            showEditSetMetricNoteDialog(
                                setMetricPair.first,
                                liftSet
                            )
                        },
                        enabled = deleteButtonsEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EditNote,
                            contentDescription = stringResource(R.string.edit_note)
                        )
                    }

                    // text field for weight
                    OutlinedTextField(
                        value = setMetricDisplayDetailMap[setMetricPair.first.id]?.value
                            ?: "",
                        onValueChange = { onSetMetricValueChanged(it, setMetricPair.first) },
                        label = {
                            Text(
                                text = liftDetail.unitName,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = firstMetricInputColor,
                            unfocusedTextColor = firstMetricInputColor,
                            focusedLabelColor = firstMetricInputColor,
                            unfocusedLabelColor = firstMetricInputColor.copy(
                                alpha = 0.75f
                            ),
                            focusedBorderColor = firstMetricInputColor,
                            unfocusedBorderColor = firstMetricInputColor.copy(
                                alpha = 0.6f
                            ),
                            cursorColor = firstMetricInputColor,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                if (liftDetail.metricType == "reps") {
                                    repFocusRequester.requestFocus()
                                } else {
                                    hourFocusRequester.requestFocus()
                                }
                            }
                        ),
                        modifier = Modifier
                            .widthIn(
                                min = 96.dp,
                                max = 140.dp
                            )
                    )
                }

                // text to hold first metric note if applicable
                if (setMetricPair.first.note.isNotBlank()) {
                    Text(
                        text = setMetricPair.first.note,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                            alpha = 0.75f
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(140.dp)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .width(140.dp)
                        .padding(top = 8.dp),
                    color = screenContentColor
                )

                // determine if the next text fields should be time or reps
                if (liftDetail.metricType == "reps") {
                    // row to hold note icon button and reps field
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        // icon button to add a note to this setmetric
                        IconButton(
                            onClick = {
                                showEditSetMetricNoteDialog(
                                    setMetricPair.second,
                                    liftSet
                                )
                            },
                            enabled = deleteButtonsEnabled
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EditNote,
                                contentDescription = stringResource(R.string.edit_note)
                            )
                        }

                        // text field for reps
                        OutlinedTextField(
                            value = setMetricDisplayDetailMap[setMetricPair.second.id]?.value
                                ?: "",
                            onValueChange = { onSetMetricValueChanged(it, setMetricPair.second) },
                            label = {
                                Text(
                                    text = stringResource(R.string.reps),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = secondMetricInputColor,
                                unfocusedTextColor = secondMetricInputColor,
                                focusedLabelColor = secondMetricInputColor,
                                unfocusedLabelColor = secondMetricInputColor.copy(
                                    alpha = 0.75f
                                ),
                                focusedBorderColor = secondMetricInputColor,
                                unfocusedBorderColor = secondMetricInputColor.copy(
                                    alpha = 0.6f
                                ),
                                cursorColor = secondMetricInputColor,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    // TODO
                                }
                            ),
                            modifier = Modifier
                                .widthIn(
                                    min = 96.dp,
                                    max = 140.dp
                                )
                                .focusRequester(repFocusRequester)
                        )
                    }
                } else {
                    // row to hold note icon button and hours field
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        // icon button to add a note to this setmetric
                        IconButton(
                            onClick = {
                                showEditSetMetricNoteDialog(
                                    setMetricPair.second,
                                    liftSet
                                )
                            },
                            enabled = deleteButtonsEnabled
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EditNote,
                                contentDescription = stringResource(R.string.edit_note)
                            )
                        }

                        // text field for hours
                        OutlinedTextField(
                            value = setMetricDisplayDetailMap[setMetricPair.second.id]?.hours
                                ?: "",
                            onValueChange = { onSetMetricTimeValueChanged(it, setMetricPair.second, "hours") },
                            label = {
                                Text(
                                    text = stringResource(R.string.hours),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = secondMetricInputColor,
                                unfocusedTextColor = secondMetricInputColor,
                                focusedLabelColor = secondMetricInputColor,
                                unfocusedLabelColor = secondMetricInputColor.copy(
                                    alpha = 0.75f
                                ),
                                focusedBorderColor = secondMetricInputColor,
                                unfocusedBorderColor = secondMetricInputColor.copy(
                                    alpha = 0.6f
                                ),
                                cursorColor = secondMetricInputColor,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    minuteFocusRequester.requestFocus()
                                    // TODO
                                }
                            ),
                            modifier = Modifier
                                .widthIn(
                                    min = 96.dp,
                                    max = 140.dp
                                )
                                .padding(
                                    bottom = 4.dp
                                )
                                .focusRequester(hourFocusRequester)
                        )
                    }
                    // text field for minutes
                    OutlinedTextField(
                        value = setMetricDisplayDetailMap[setMetricPair.second.id]?.minutes
                            ?: "",
                        onValueChange = { onSetMetricTimeValueChanged(it, setMetricPair.second, "minutes") },
                        label = {
                            Text(
                                text = stringResource(R.string.minutes),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = secondMetricInputColor,
                            unfocusedTextColor = secondMetricInputColor,
                            focusedLabelColor = secondMetricInputColor,
                            unfocusedLabelColor = secondMetricInputColor.copy(
                                alpha = 0.75f
                            ),
                            focusedBorderColor = secondMetricInputColor,
                            unfocusedBorderColor = secondMetricInputColor.copy(
                                alpha = 0.6f
                            ),
                            cursorColor = secondMetricInputColor,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                secondFocusRequester.requestFocus()
                                // TODO
                            }
                        ),
                        modifier = Modifier
                            .widthIn(
                                min = 96.dp,
                                max = 140.dp
                            )
                            .padding(
                                bottom = 4.dp
                            )
                            .focusRequester(minuteFocusRequester)
                    )
                    // text field for seconds
                    OutlinedTextField(
                        value = setMetricDisplayDetailMap[setMetricPair.second.id]?.seconds
                            ?: "",
                        onValueChange = { onSetMetricTimeValueChanged(it, setMetricPair.second, "seconds") },
                        label = {
                            Text(
                                text = stringResource(R.string.seconds),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = secondMetricInputColor,
                            unfocusedTextColor = secondMetricInputColor,
                            focusedLabelColor = secondMetricInputColor,
                            unfocusedLabelColor = secondMetricInputColor.copy(
                                alpha = 0.75f
                            ),
                            focusedBorderColor = secondMetricInputColor,
                            unfocusedBorderColor = secondMetricInputColor.copy(
                                alpha = 0.6f
                            ),
                            cursorColor = secondMetricInputColor,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                // TODO
                            }
                        ),
                        modifier = Modifier
                            .widthIn(
                                min = 96.dp,
                                max = 140.dp
                            )
                            .focusRequester(secondFocusRequester)
                    )
                }

                // text to hold second metric note if applicable
                if (setMetricPair.second.note.isNotBlank()) {
                    Text(
                        text = setMetricPair.second.note,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                            alpha = 0.75f
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(140.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExistingLiftEntryCard(
    liftSuggestionsList: List<LiftSearchDetail>,
    inputLiftName: String,
    onInputLiftValueChanged: (String) -> Unit,
    onClickCancel: () -> Unit,
    dismissDropdownSuggestionList: () -> Unit,
    liftSuggestionClicked: (Int) -> Unit,
    dropdownButtonClicked: () -> Unit,
    onGo: () -> Unit,
    screenContentColor: Color,
    deleteButtonsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val liftNameFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        delay(100)
        liftNameFocusRequester.requestFocus()
        keyboardController?.show()
    }

    Card(
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = modifier
            .fillMaxWidth()
    ) {
        // column to hold card contents
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = liftSuggestionsList.isNotEmpty(),
                onExpandedChange = {}
            ) {
                // text field for inputting existing lift name
                OutlinedTextField(
                    value = inputLiftName,
                    onValueChange = onInputLiftValueChanged,
                    label = {
                        Text(stringResource(R.string.existing_lift_name))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = { onGo() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = screenContentColor,
                        unfocusedTextColor = screenContentColor,
                        focusedLabelColor = screenContentColor,
                        unfocusedLabelColor = screenContentColor.copy(alpha = 0.75f),
                        focusedBorderColor = screenContentColor,
                        unfocusedBorderColor = screenContentColor.copy(alpha = 0.6f),
                        cursorColor = screenContentColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = dropdownButtonClicked,
                            enabled = deleteButtonsEnabled
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = stringResource(R.string.show_all_lifts),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                            enabled = true
                        )
                        .focusRequester(liftNameFocusRequester)
                        .padding(
                            bottom = 4.dp,
                            end = 4.dp
                        )
                )

                ExposedDropdownMenu(
                    expanded = liftSuggestionsList.isNotEmpty(),
                    onDismissRequest = dismissDropdownSuggestionList
                ) {
                    liftSuggestionsList.forEach { liftSearchDetail ->
                        DropdownMenuItem(
                            text = {
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.Top,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    // column to hold lift name and note and lift detail flow row
                                    Column {
                                        CompositionLocalProvider(
                                            LocalContentColor provides if (liftSearchDetail.selected) {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onBackground
                                            }
                                        ) {
                                            // lift name
                                            Text(
                                                text = liftSearchDetail.liftObj.name,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            // lift note (if applicable)
                                            if (liftSearchDetail.liftObj.note.isNotBlank()) {
                                                Text(
                                                    text = liftSearchDetail.liftObj.note,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontSize = 13.sp,
                                                    color = if (liftSearchDetail.selected) {
                                                        MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                            alpha = 0.75f
                                                        )
                                                    } else {
                                                        MaterialTheme.colorScheme.onBackground.copy(
                                                            alpha = 0.75f
                                                        )
                                                    }
                                                )
                                            }
                                            // flow row to display muscle group name, metric type, and unit name
                                            LiftDetailFlowRow(
                                                muscleGroupName = liftSearchDetail.muscleGroupName,
                                                metricType = liftSearchDetail.metricType,
                                                unitName = liftSearchDetail.unitName,
                                                modifier = Modifier.padding(
                                                    top = 4.dp
                                                ),
                                                tintColor = LocalContentColor.current
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = { liftSuggestionClicked(liftSearchDetail.liftObj.id) },
                            modifier = Modifier.background(
                                if (liftSearchDetail.selected) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    Color.Transparent
                                }
                            )
                        )
                    }
                }
            }

            // button to cancel
            OutlinedButton(
                onClick = onClickCancel,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = screenContentColor
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = screenContentColor.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.simple_cancel))
            }
        }
    }
}

@Composable
fun HorizontalAddButtonDivider(
    onClickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))

        Button(
            onClick = onClickAdd,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_lift)
            )
        }

        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}
