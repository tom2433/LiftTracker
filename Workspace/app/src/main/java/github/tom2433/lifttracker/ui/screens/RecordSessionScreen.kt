package github.tom2433.lifttracker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import github.tom2433.lifttracker.R
import github.tom2433.lifttracker.data.structures.LiftSearchDetail
import github.tom2433.lifttracker.ui.AppViewModelProvider
import github.tom2433.lifttracker.ui.navigation.NavigationDestination
import github.tom2433.lifttracker.ui.utils.LiftDetailFlowRow
import github.tom2433.lifttracker.ui.utils.ShowElementEntryDialog
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionInProgressScreen(
    viewModel: RecordSessionViewModel,
    recordSessionUiState: RecordSessionUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
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
                    // row to hold play icon and name/note for current day
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                onClick = {
                                    viewModel.showDayEditDialog()
                                }
                            )
                    ) {
                        // play icon
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Session in progress",
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

                        // column to hold day name/note
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            // day name
                            Text(
                                text = recordSessionUiState.activeLiftDay?.day_label ?: "null",
                                style = MaterialTheme.typography.titleMedium
                            )

                            // day note (if applicable)
                            if (recordSessionUiState.activeLiftDay?.note?.isNotBlank() ?: false) {
                                Text(
                                    text = recordSessionUiState.activeLiftDay.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    // finish session button
                    Button(
                        onClick = { viewModel.finishSession() },
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
                        // lifts in progress will go here

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
                                onGo = { viewModel.onGoLiftEntry() },
                                screenContentColor = screenContentColor,
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

    if (recordSessionUiState.dayEditDialogVisible) {
        ShowElementEntryDialog(
            dialogTitle = stringResource(R.string.edit_session_dialog_title),
            submitBtnText = stringResource(R.string.update_session),
            elementNameInputLabel = stringResource(R.string.session_name),
            elementNoteInputLabel = stringResource(R.string.session_note_optional),
            buttonEnabled = viewModel.validateDayInput(),
            newElementName = recordSessionUiState.newDayName,
            newElementNote = recordSessionUiState.newDayNote,
            onElementNameValueChanged = { viewModel.updateNewDayName(it) },
            onElementNoteValueChanged = { viewModel.updateNewDayNote(it) },
            onSubmit = { viewModel.updateDayNameAndNote() },
            onDismissRequest = { viewModel.dismissDayEditDialog() }
        )
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
    onGo: () -> Unit,
    screenContentColor: Color,
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
                // textfield for inputting existing lift name
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
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                            enabled = true
                        )
                        .fillMaxWidth()
                        .focusRequester(liftNameFocusRequester)
                        .padding(bottom = 4.dp)
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
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                            // flow row to display muscle group name, metric type, and unit name
                                            LiftDetailFlowRow(
                                                muscleGroupName = liftSearchDetail.muscleGroupName,
                                                metricType = liftSearchDetail.metricType,
                                                unitName = liftSearchDetail.unitName,
                                                modifier = Modifier.padding(
                                                    top = 4.dp
                                                )
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