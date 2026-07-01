package com.example.lifttracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
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
            // clickable card to add a lift
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 36.dp)
                    .height(IntrinsicSize.Min)
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
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