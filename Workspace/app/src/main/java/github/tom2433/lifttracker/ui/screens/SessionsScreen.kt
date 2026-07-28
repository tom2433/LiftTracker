package github.tom2433.lifttracker.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import github.tom2433.lifttracker.R
import github.tom2433.lifttracker.data.structures.SessionDetail
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
import github.tom2433.lifttracker.ui.AppViewModelProvider
import github.tom2433.lifttracker.ui.navigation.NavigationDestination
import github.tom2433.lifttracker.ui.utils.DateRangePickerModal
import github.tom2433.lifttracker.ui.utils.LabelHeader
import github.tom2433.lifttracker.ui.utils.LayoutSwitcher
import github.tom2433.lifttracker.ui.utils.LoadMoreLabelAndButton
import github.tom2433.lifttracker.ui.utils.MuscleGroupDonutChart
import github.tom2433.lifttracker.ui.utils.ShowElementDeleteDialog
import github.tom2433.lifttracker.ui.utils.ShowElementEntryDialog
import github.tom2433.lifttracker.ui.utils.ThreeDotMenu
import github.tom2433.lifttracker.ui.viewModels.SessionsViewModel

object SessionsDestination : NavigationDestination {
    override val route = "sessions"
    override val titleRes = R.string.sessions_title
}

object TimeFrameOption {
    const val ALL_TIME: String = "All time"
    const val PAST_WEEK: String = "Past week"
    const val PAST_TWO_WEEKS: String = "Past 2 weeks"
    const val PAST_MONTH: String = "Past month"
    const val CHOOSE_DATE: String = "Custom date range"
}

/**
 * Entry route for Sessions Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SessionsScreen(
    modifier: Modifier = Modifier,
    viewModel: SessionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val sessionsUiState by viewModel.sessionsUiState.collectAsState()
    val beginDateLabel = calculateBeginDateLabel(sessionsUiState.startDate)
    val endDateLabel = calculateEndDateLabel(sessionsUiState.endDate)
    val startDateDropdownOptions: List<String> = listOf(
        TimeFrameOption.ALL_TIME,
        TimeFrameOption.PAST_WEEK,
        TimeFrameOption.PAST_TWO_WEEKS,
        TimeFrameOption.PAST_MONTH,
        TimeFrameOption.CHOOSE_DATE
    )
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    // column to hold all sessions screen contents
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // row to hold dropdown menu box for timeframe selector
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = sessionsUiState.timeFrameDropdownExpanded,
                onExpandedChange = {}
            ) {
                // row to hold timeframe selector
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(
                            onClick = { viewModel.toggleTimeFrameDropdown() }
                        )
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable
                        )
                ) {
                    Text(
                        text = sessionsUiState.timeFrameLabel,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = stringResource(R.string.select_start_date)
                    )
                }

                // start date dropdown menu
                ExposedDropdownMenu(
                    expanded = sessionsUiState.timeFrameDropdownExpanded,
                    onDismissRequest = { viewModel.dismissTimeFrameDropdown() }
                ) {
                    startDateDropdownOptions.forEach { startDateLabel ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = startDateLabel,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = { viewModel.timeFrameMenuOptionClicked(startDateLabel) }
                        )
                    }
                }
            }
        }

        MuscleGroupDonutChart(
            muscleGroupFrequencyList = sessionsUiState.muscleGroupFrequencyList,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // text to inform user of current timeframe
        Text(
            text = "$beginDateLabel - $endDateLabel",
            color = MaterialTheme.colorScheme.onBackground.copy(
                alpha = 0.75f
            ),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(
                top = 8.dp
            )
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 8.dp,
                    bottom = 32.dp
                ),
            color = MaterialTheme.colorScheme.onBackground.copy(
                alpha = 0.5f
            )
        )

        // list of session cards
        loop@ for ((index, weekPair) in sessionsUiState.weekStringPairList.withIndex()) {
            // row to hold label for this week
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // column to hold sessions for this week
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // animated visibility for the week label and layout switcher if first index
                    AnimatedVisibility(
                        visible = sessionsUiState.sessionDetailMap[weekPair.second[0]]?.visible ?: continue@loop,
                        enter = fadeIn(tween(300)),
                        exit = fadeOut(tween(300))
                    ) {
                        // row to hold week label and layout switcher if first index
                        Row(
                            horizontalArrangement = if (index == 0) {
                                Arrangement.SpaceBetween
                            } else {
                                Arrangement.Start
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = weekPair.first,
                                color = MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = 0.75f
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (index == 0) {
                                LayoutSwitcher(
                                    onListLayoutClicked = { viewModel.toggleListLayout() },
                                    listLayoutEnabled = !sessionsUiState.donutChartsVisible,
                                )
                            }
                        }
                    }

                    // loop to display all session cards for this week
                    id_loop@ for (sessionId in weekPair.second) {
                        // key to differentiate session cards
                        key(sessionId) {
                            val sessionDetail: SessionDetail =
                                sessionsUiState.sessionDetailMap[sessionId] ?: continue@id_loop

                            // animated visibility for each session card
                            AnimatedVisibility(
                                visible = sessionDetail.visible,
                                enter = slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(300)
                                ) + fadeIn(
                                    animationSpec = tween(300)
                                ),
                                exit = slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(300)
                                ) + fadeOut(
                                    animationSpec = tween(300)
                                )
                            ) {
                                SessionCard(
                                    sessionDetail = sessionDetail,
                                    donutChartsVisible = sessionsUiState.donutChartsVisible,
                                    onClickThreeDotMenu = {
                                        viewModel.threeDotMenuClicked(
                                            sessionCardId = sessionDetail.sessionId
                                        )
                                    },
                                    onDismissThreeDotMenu = {
                                        viewModel.dismissThreeDotMenus()
                                    },
                                    onClickSwitchToInProgress = {
                                        viewModel.switchSessionToInProgress(
                                            sessionCardId = sessionDetail.sessionId
                                        )
                                    },
                                    onClickFinishSession = {
                                        viewModel.finishSession(sessionDetail.sessionId)
                                    },
                                    onClickEditSession = {
                                        viewModel.showEditSessionDialog(sessionDetail.sessionId)
                                    },
                                    onClickDeleteSession = {
                                        viewModel.showDeleteSessionDialog(sessionDetail.sessionId)
                                    },
                                    onClickCard = {
                                        viewModel.toggleCardSelected(sessionDetail.sessionId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // prompt user to load more sessions if applicable
        LoadMoreLabelAndButton(
            numDisplayed = sessionsUiState.sessionDetailMap.keys.size,
            numExisting = sessionsUiState.numSessionsInTimeFrame,
            elementNamePlural = "sessions",
            onClickLoadMore = { viewModel.loadMoreSessions(it) }
        )
    }

    // show date range picker dialog if applicable
    if (sessionsUiState.dateRangePickerVisible) {
        DateRangePickerModal(
            onDateRangeSelected = { datePair ->
                viewModel.customDatePicked(
                    startDateMillis = datePair.first,
                    endDateMillis = datePair.second
                )
            },
            onDismiss = { viewModel.dismissDateRangePicker() }
        )
    }
    
    // show delete session dialog if applicable
    if (sessionsUiState.deleteSessionDialogVisible && sessionsUiState.sessionToDelete != null) {
        val sessionName: String = sessionsUiState.sessionToDelete!!.session_label
        val sessionDate: String = DateTimeCalculator.convertIsoDateToReadableFormat(
            isoDate = sessionsUiState.sessionToDelete!!.date
        )

        ShowElementDeleteDialog(
            dialogTitle = "Delete '${sessionName}'?",
            warningDescription = "You are about to delete '${sessionName}', which was completed on ${sessionDate}. This will delete all set data completed during this session. You cannot undo this action.",
            deleteBtnText = "Delete $sessionName",
            onDismissRequest = { viewModel.dismissDeleteSessionDialog() },
            onDelete = { viewModel.deleteSession() }
        )
    }
    
    // show edit session dialog if applicable
    if (sessionsUiState.editSessionDialogVisible && sessionsUiState.sessionToEdit != null) {
        val formattedDate: String = DateTimeCalculator.convertIsoDateToReadableFormat(sessionsUiState.sessionToEdit!!.date)
        val dialogTitle = "Edit '${sessionsUiState.sessionToEdit!!.session_label}' from ${formattedDate}:"
        val submitBtnText = "Update '${sessionsUiState.sessionToEdit!!.session_label}'"

        ShowElementEntryDialog(
            dialogTitle = dialogTitle,
            submitBtnText = submitBtnText,
            elementNameInputLabel = stringResource(R.string.session_name),
            elementNoteInputLabel = stringResource(R.string.session_note_optional),
            buttonEnabled = viewModel.validateSessionEntry(),
            newElementName = sessionsUiState.newSessionName,
            newElementNote = sessionsUiState.newSessionNote,
            onElementNameValueChanged = { viewModel.updateNewSessionName(it) },
            onElementNoteValueChanged = { viewModel.updateNewSessionNote(it) },
            onSubmit = { viewModel.updateSession() },
            onDismissRequest = { viewModel.dismissEditSessionDialog() },
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SessionCard(
    sessionDetail: SessionDetail,
    donutChartsVisible: Boolean,
    onClickThreeDotMenu: () -> Unit,
    onDismissThreeDotMenu: () -> Unit,
    onClickSwitchToInProgress: () -> Unit,
    onClickFinishSession: () -> Unit,
    onClickEditSession: () -> Unit,
    onClickDeleteSession: () -> Unit,
    onClickCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val noteColor = CardDefaults.cardColors().contentColor.copy(
        alpha = 0.75f
    )

    // Column to hold session's card and animated content below it
    Column(
        modifier = modifier
    ) {
        // card to hold labels and three dot menu on top, donut chart on bottom if applicable
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 16.dp),
            border = if (sessionDetail.sessionInProgress) {
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                null
            },
            onClick = onClickCard
        ) {
            // Column to hold session card contents:
            // labels/three dot menu on top, donut chart on bottom
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // if session is in progress, display label that the session is in progress
                // and provide a "finish" button
                if (sessionDetail.sessionInProgress) {
                    LabelHeader(
                        headerText = stringResource(R.string.in_progress),
                        color = MaterialTheme.colorScheme.primary
                    )
                    FinishButton(
                        onClick = onClickFinishSession
                    )
                }

                // row to hold session label info and three dot menu
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column {
                        // session label
                        Text(
                            text = sessionDetail.sessionName,
                            style = MaterialTheme.typography.titleLarge
                        )
                        // session note (if applicable)
                        if (sessionDetail.sessionNote.isNotBlank()) {
                            Text(
                                text = sessionDetail.sessionNote,
                                color = noteColor,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 13.sp
                            )
                        }
                        // session date (readable format)
                        Text(
                            text = DateTimeCalculator.convertIsoDateToReadableFormat(
                                isoDate = sessionDetail.sessionDateIso
                            ),
                            color = noteColor,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 13.sp
                        )
                    }
                    // three dot menu to set lift to "in progress" or delete
                    ThreeDotMenu(
                        contentDescRes = R.string.session_menu,
                        element1TextRes = R.string.change_session_to_in_progress,
                        element2TextRes = R.string.edit_session,
                        element3TextRes = R.string.delete_session,
                        expanded = sessionDetail.menuExpanded,
                        onClickDots = onClickThreeDotMenu,
                        onClickElement1 = onClickSwitchToInProgress,
                        onClickElement2 = onClickEditSession,
                        onClickElement3 = onClickDeleteSession,
                        onDismissRequest = onDismissThreeDotMenu,
                    )
                }

                // bottom of column: donut chart for muscle groups in this session
                // animated content since the user may select to not see these
                AnimatedVisibility(
                    visible = donutChartsVisible || sessionDetail.selected,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(300)
                    ) + fadeIn(tween(300)),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(300)
                    ) + fadeOut(tween(300))
                ) {
                    MuscleGroupDonutChart(
                        muscleGroupFrequencyList = sessionDetail.liftSetCountPerMuscleGroupList,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        height = 150.dp,
                        innerHeight = 75.dp
                    )
                }
            }
        }
    }
}

@Composable
fun FinishButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "Finish"
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateBeginDateLabel(startDate: String?): String {
    return if (startDate == null) {
        "Beginning of time"
    } else {
        DateTimeCalculator.convertIsoDateToReadableFormat(startDate)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateEndDateLabel(endDate: String?): String {
    return if (endDate == null) {
        "Today"
    } else {
        DateTimeCalculator.convertIsoDateToReadableFormat(endDate)
    }
}
