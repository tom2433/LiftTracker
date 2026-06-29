package com.example.lifttracker.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifttracker.R
import com.example.lifttracker.data.MuscleGroup
import com.example.lifttracker.data.Profile
import com.example.lifttracker.ui.AppViewModelProvider
import com.example.lifttracker.ui.navigation.NavigationDestination
import com.example.lifttracker.ui.theme.LiftTrackerTheme
import com.example.lifttracker.ui.utils.WelcomeDialog
import com.example.lifttracker.ui.viewModels.MuscleGroupsViewModel

object MuscleGroupsDestination : NavigationDestination {
    override val route = "muscleGroups"
    override val titleRes = R.string.muscle_groups_title
}

/**
 * Entry route for Home Screen
 */
@Composable
fun MuscleGroupsScreen(
    modifier: Modifier = Modifier,
    viewModel: MuscleGroupsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val muscleGroupsUiState by viewModel.muscleGroupsUiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current

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
                .verticalScroll(rememberScrollState())
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
            for (muscleGroupDetail in muscleGroupsUiState.muscleGroupList) {
                // each muscle group has card design
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 168.dp)
                        .padding(bottom = 16.dp),
                    onClick = { /* TODO */ }
                ) {
                    // column to hold card contents, 16.dp padding
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // muscle group name (title)
                        Text(
                            text = muscleGroupDetail.name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = if (muscleGroupDetail.note.isBlank()) {
                                Modifier.padding(bottom = 32.dp)
                            } else {
                                Modifier
                            }
                        )

                        // muscle group note (if applicable)
                        if (muscleGroupDetail.note.isNotBlank()) {
                            Text(
                                text = muscleGroupDetail.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )
                        }

                        // muscle group stats
                        // # of lifts
                        MuscleGroupDetailRow(
                            label = R.string.num_of_lifts_label,
                            value = muscleGroupDetail.numLifts.toString()
                        )

                        // # of days trained (total)
                        MuscleGroupDetailRow(
                            label = R.string.num_of_sessions_trained_label,
                            value = muscleGroupDetail.numSessions.toString()
                        )

                        // avg # of sessions/week
                        MuscleGroupDetailRow(
                            label = R.string.avg_num_sessions_per_week_label,
                            value = "%.2f".format(muscleGroupDetail.avgNumSessionsPerWeek)
                        )

                        // avg # of sets/session
                        MuscleGroupDetailRow(
                            label = R.string.avg_num_sets_per_session_label,
                            value = "%.2f".format(muscleGroupDetail.avgNumSetsPerSession)
                        )

                        // avg # of sets/week
                        MuscleGroupDetailRow(
                            label = R.string.avg_num_sets_per_week_label,
                            value = "%.2f".format(muscleGroupDetail.avgNumSetsPerWeek)
                        )

                        // avg # of reps/set
                        MuscleGroupDetailRow(
                            label = R.string.avg_num_reps_per_set_label,
                            value = if (muscleGroupDetail.avgNumRepsPerSet != -1.0) {
                                "%.2f".format(muscleGroupDetail.avgNumRepsPerSet)
                            } else {
                                "N/A"
                            }
                        )

                        // last date trained (Today, Yesterday, 2 days ago, 3, ..., 6, 1 week ago,
                        // 2 weeks ago, ..., 1 month ago, Over 1 month ago, 2 months ago, 3, 4, ...,
                        // 1 year ago, Over 1 year ago, Never)
                        MuscleGroupDetailRow(
                            label = R.string.last_date_trained_label,
                            value = muscleGroupDetail.lastDateTrained
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MuscleGroupDetailRow(
    @StringRes label: Int,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // label for detail metric
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodyMedium
        )

        // divider to link label to metric
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        )

        // metric
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
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