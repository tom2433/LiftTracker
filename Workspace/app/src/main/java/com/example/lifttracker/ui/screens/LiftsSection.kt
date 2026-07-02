package com.example.lifttracker.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifttracker.R
import com.example.lifttracker.ui.AppViewModelProvider
import com.example.lifttracker.ui.utils.ShowLiftEntryDialog
import com.example.lifttracker.ui.viewModels.LiftsViewModel

@Composable
fun LiftSection(
    muscleGroupId: Int,
    modifier: Modifier = Modifier,
    viewModel: LiftsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val liftsUiState by viewModel.liftsUiState.collectAsState()

    // need to retrieve a list of lifts that belong to this specific muscle group
    viewModel.initializeLiftList(muscleGroupId)

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = 16.dp,
                bottom = 16.dp,
                start = 32.dp,
                end = 32.dp
            )
    ) {
//        // add all lift cards here
//        for (lift in liftsUiState.liftList) {
//            Card(
//                shape = RoundedCornerShape(16.dp),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .defaultMinSize(minHeight = 40.dp),
//                onClick = { /* TODO: Lift Card click */ }
//            ) {
//                // Row to hold card contents (name/note on left, three dot menu on right)
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp)
//                ) {
//                    // Column to hold name and note
//                    Column() {
//                        // lift name
//                        Text(
//                            text = lift.name,
//                            style = MaterialTheme.typography.titleMedium
//                        )
//                        // lift note (if applicable)
//                        if (lift.note.isNotBlank()) {
//                            Text(
//                                text = lift.note,
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.outline
//                            )
//                        }
//                    }
//
//                    // box to hold 3 dot menu
//                    Box {
//                        // three dot icon
//                        IconButton(
//                            onClick = { /* TODO: lift menu click */ }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.MoreVert,
//                                contentDescription = stringResource(R.string.lift_menu)
//                            )
//                        }
//
//                        // drop down menu
//                        DropdownMenu(
//                            expanded = false, // TODO
//                            onDismissRequest = { /* TODO */ }
//                        ) {
//                            // menu item for edit
//                            DropdownMenuItem(
//                                text = { Text(stringResource(R.string.edit)) },
//                                onClick = { /* TODO */ }
//                            )
//                            // menu item for delete
//                            DropdownMenuItem(
//                                text = { Text(stringResource(R.string.delete)) },
//                                onClick = { /* TODO */ }
//                            )
//                        }
//                    }
//                }
//            }
//        }
    }

    // add button outside of above column
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
                viewModel.dismissAddLiftDialog()
            },
        )
    }
}