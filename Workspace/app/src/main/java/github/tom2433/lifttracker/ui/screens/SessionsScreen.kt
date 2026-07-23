package github.tom2433.lifttracker.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import github.tom2433.lifttracker.R
import github.tom2433.lifttracker.data.musclegroup.MuscleGroup
import github.tom2433.lifttracker.data.structures.SessionDetail
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
import github.tom2433.lifttracker.ui.AppViewModelProvider
import github.tom2433.lifttracker.ui.navigation.NavigationDestination
import github.tom2433.lifttracker.ui.utils.DateRangePickerModal
import github.tom2433.lifttracker.ui.utils.MuscleGroupDonutChart
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

    // column to hold all sessions screen contents
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
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
        loop@ for (weekPair in sessionsUiState.weekStringPairList) {
            // row to hold label for this week
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                ) {
                    AnimatedVisibility(
                        visible = sessionsUiState.sessionDetailMap[weekPair.second[0]]?.visible ?: continue@loop,
                        enter = fadeIn(tween(300)),
                        exit = fadeOut(tween(300))
                    ) {
                        Text(
                            text = weekPair.first,
                            color = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.75f
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    for (sessionId in weekPair.second) {
                        val sessionDetail: SessionDetail =
                            sessionsUiState.sessionDetailMap[sessionId] ?: continue

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
                                sessionDetail = sessionDetail
                            )
                        }
                    }
                }
            }
        }
    }

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
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SessionCard(
    sessionDetail: SessionDetail,
    modifier: Modifier = Modifier
) {
    val noteColor = CardDefaults.cardColors().contentColor.copy(
        alpha = 0.75f
    )

    // Column to hold session's card and animated content below it
    Column {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 16.dp),
            onClick = { /* TODO: Session Card clicked */ }
        ) {
            // Column to hold session card contents:
            // labels on top, donut chart on bottom
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
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

                // bottom of column: donut chart for muscle groups in this session
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
