package github.tom2433.lifttracker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import github.tom2433.lifttracker.ui.AppViewModelProvider
import github.tom2433.lifttracker.ui.navigation.NavigationDestination
import github.tom2433.lifttracker.ui.utils.ShowElementEntryDialog
import github.tom2433.lifttracker.ui.viewModels.RecordSessionUiState
import github.tom2433.lifttracker.ui.viewModels.RecordSessionViewModel

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

@Composable
fun SessionInProgressScreen(
    viewModel: RecordSessionViewModel,
    recordSessionUiState: RecordSessionUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = "container"
                    ),
                    animatedVisibilityScope = animatedVisibilityScope
                )
        ) {
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
                    OutlinedButton(
                        onClick = { viewModel.finishSession() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Finish")
                    }
                }

                // divider to separate scrollable content
                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                // implement scrollable content here
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