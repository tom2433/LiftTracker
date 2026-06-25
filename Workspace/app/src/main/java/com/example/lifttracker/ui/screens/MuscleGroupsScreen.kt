package com.example.lifttracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifttracker.LiftTrackerDrawer
import com.example.lifttracker.R
import com.example.lifttracker.ui.AppViewModelProvider
import com.example.lifttracker.ui.navigation.NavigationDestination
import com.example.lifttracker.ui.theme.LiftTrackerTheme
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
    navigateToRecordSession: () -> Unit,
    navigateToMuscleGroups: () -> Unit,
    navigateToSessions: () -> Unit,
    navigateToCalendar: () -> Unit,
    navigateToAnalytics: () -> Unit,
    navigateToTools: () -> Unit,
    navigateToSettings: () -> Unit,
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
        LiftTrackerDrawer(
            titleRes = MuscleGroupsDestination.titleRes,
            profiles = muscleGroupsUiState.profileList,
            navigateToRecordSession = navigateToRecordSession,
            navigateToMuscleGroups = navigateToMuscleGroups,
            navigateToSessions = navigateToSessions,
            navigateToCalendar = navigateToCalendar,
            navigateToAnalytics = navigateToAnalytics,
            navigateToTools = navigateToTools,
            navigateToSettings = navigateToSettings
        ) {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Muscle groups will show up here, we also need a FAB"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeDialog(
    buttonEnabled: Boolean,
    newProfileName: String,
    newProfileNote: String,
    onProfileNameValueChanged: (String) -> Unit,
    onProfileNoteValueChanged: (String) -> Unit,
    onCreateProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
    ) {
        Card(
            modifier = modifier
                .wrapContentSize()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // welcome title
                Text(
                    text = stringResource(R.string.welcome_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // divider
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // profile name input
                TextField(
                    value = newProfileName,
                    onValueChange = onProfileNameValueChanged,
                    label = {
                        Text(stringResource(R.string.profile_name_input_label))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Label,
                            contentDescription = stringResource(R.string.profile_name_input_label)
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                )

                // profile note input
                TextField(
                    value = newProfileNote,
                    onValueChange = onProfileNoteValueChanged,
                    label = {
                        Text(stringResource(R.string.profile_note_input_label))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = stringResource(R.string.profile_note_input_label)
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                )

                // divider
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // welcome description (tell user to create one profile)
                Text(
                    text = stringResource(R.string.welcome_description),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // divider
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // button to create profile
                Button(
                    onClick = onCreateProfile,
                    enabled = buttonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.create_profile_btn_text))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeDialogPreview() {
    LiftTrackerTheme(dynamicColor = false, darkTheme = true) {
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