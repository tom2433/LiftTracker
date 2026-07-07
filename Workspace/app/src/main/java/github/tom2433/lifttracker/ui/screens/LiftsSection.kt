package github.tom2433.lifttracker.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import github.tom2433.lifttracker.ui.utils.ShowElementDeleteDialog
import github.tom2433.lifttracker.ui.utils.ShowLiftEntryDialog
import github.tom2433.lifttracker.ui.utils.ThreeDotMenu
import github.tom2433.lifttracker.ui.viewModels.LiftsViewModel

@Composable
fun LiftSection(
    muscleGroupId: Int,
    goToLiftScreen: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    viewModel: LiftsViewModel = viewModel(
        key = "lifts_$muscleGroupId",
        factory = AppViewModelProvider.liftsFactory(muscleGroupId)
    )
) {
    val liftsUiState by viewModel.liftsUiState.collectAsState()

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // add all lift cards here
        for ((liftId, liftDetail) in liftsUiState.liftMap) {
            with (sharedTransitionScope) {
                // each lift has card format
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 16.dp)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(
                                key = liftId.toString()
                            ),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    onClick = { goToLiftScreen(liftId) }
                ) {
                    // Row to hold card contents (name/note on left, three dot menu on right)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .padding(
                                start = 8.dp,
                                end = 0.dp,
                                top = 4.dp,
                                bottom = 4.dp
                            )
                    ) {
                        // Column to hold name and note
                        Column {
                            // lift name
                            Text(
                                text = liftDetail.liftObj.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(
                                        key = "$liftId-name"
                                    ),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            )
                            // lift note (if applicable)
                            if (liftDetail.liftObj.note.isNotBlank()) {
                                Text(
                                    text = liftDetail.liftObj.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.sharedElement(
                                        sharedContentState = rememberSharedContentState(
                                            key = "$liftId-note"
                                        ),
                                        animatedVisibilityScope = animatedVisibilityScope
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        ThreeDotMenu(
                            contentDescRes = R.string.lift_menu,
                            element1TextRes = R.string.edit,
                            element2TextRes = R.string.delete,
                            expanded = liftDetail.threeDotMenuOpen,
                            onClickDots = {
                                viewModel.threeDotMenuClicked(liftId)
                            },
                            onClickElement1 = {
                                viewModel.showEditLiftDialog(liftId)
                            },
                            onClickElement2 = {
                                viewModel.showDeleteLiftDialog(liftId)
                            },
                            onDismissRequest = {
                                viewModel.threeDotMenuClicked(liftId)
                            },
                            modifier = Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(
                                    key = "$liftId-menu"
                                ),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )
                    }
                }
            }
        }
    }

    // add button outside of above Flow Row
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
        onClick = { viewModel.showAddLiftDialog() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_lift)
            )
        }
    }

    if (liftsUiState.userIsEditingLift) {
        // show edit lift dialog
        ShowLiftEntryDialog(
            dialogTitle = "Edit '${liftsUiState.liftToEdit?.name ?: "null"}' in ${liftsUiState.muscleGroup?.name ?: "null"}",
            submitBtnText = stringResource(R.string.update_lift),
            buttonEnabled = viewModel.validateLift(),
            newLiftName = liftsUiState.newLiftName,
            newLiftNote = liftsUiState.newLiftNote,
            newLiftUnitName = liftsUiState.newLiftUnitName,
            onLiftNameValueChanged = {
                viewModel.updateLiftName(it)
            },
            onLiftNoteValueChanged = {
                viewModel.updateLiftNote(it)
            },
            repsSelected = liftsUiState.newLiftMetricType == 1,
            onRepsSelected = {
                viewModel.selectReps()
            },
            timeSelected = liftsUiState.newLiftMetricType == 2,
            onTimeSelected = {
                viewModel.selectTime()
            },
            unitList = liftsUiState.unitList,
            onUnitValueChanged = {
                viewModel.updateUnit(it)
            },
            onSubmit = { viewModel.updateLift() },
            onDismissRequest = { viewModel.dismissLiftEntryDialog() }
        )
    }

    if (liftsUiState.userIsAddingLift) {
        // show add lift dialog
        ShowLiftEntryDialog(
            dialogTitle = "Add a Lift For ${liftsUiState.muscleGroup?.name ?: "null"}",
            submitBtnText = "Create Lift For ${liftsUiState.muscleGroup?.name ?: "null"}",
            buttonEnabled = viewModel.validateLift(),
            newLiftName = liftsUiState.newLiftName,
            newLiftNote = liftsUiState.newLiftNote,
            newLiftUnitName = liftsUiState.newLiftUnitName,
            onLiftNameValueChanged = {
                viewModel.updateLiftName(it)
            },
            onLiftNoteValueChanged = {
                viewModel.updateLiftNote(it)
            },
            repsSelected = liftsUiState.newLiftMetricType == 1,
            onRepsSelected = {
                viewModel.selectReps()
            },
            timeSelected = liftsUiState.newLiftMetricType == 2,
            onTimeSelected = {
                viewModel.selectTime()
            },
            unitList = liftsUiState.unitList,
            onUnitValueChanged = {
                viewModel.updateUnit(it)
            },
            onSubmit = {
                viewModel.addLift()
            },
            onDismissRequest = {
                viewModel.dismissLiftEntryDialog()
            },
        )
    }

    if (liftsUiState.userIsDeletingLift) {
        ShowElementDeleteDialog(
            dialogTitle = "Delete '${liftsUiState.liftToDelete?.name ?: "null"}' from '${liftsUiState.muscleGroup?.name ?: "null"}'?",
            warningDescription = R.string.delete_lift_warning,
            deleteBtnText = R.string.delete_lift_btn_text,
            onDismissRequest = { viewModel.dismissDeleteLiftDialog() },
            onDelete = { viewModel.deleteLift() },
        )
    }
}