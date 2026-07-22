package github.tom2433.lifttracker.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.lifecycle.viewmodel.compose.viewModel
import github.tom2433.lifttracker.R
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
    val beginDateLabel: String = if (sessionsUiState.startDate == null) {
        "Beginning of time"
    } else {
        DateTimeCalculator.convertIsoDateToReadableFormat(sessionsUiState.startDate!!)
    }
    val endDateLabel: String = if (sessionsUiState.endDate == null) {
        "Today"
    } else {
        DateTimeCalculator.convertIsoDateToReadableFormat(sessionsUiState.endDate!!)
    }
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
            modifier = Modifier.padding(vertical = 8.dp)
        )
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
