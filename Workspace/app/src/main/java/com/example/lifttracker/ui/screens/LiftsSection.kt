package com.example.lifttracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.util.TableInfo
import com.example.lifttracker.data.MuscleGroup
import com.example.lifttracker.ui.AppViewModelProvider
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
        // horizontal divider
        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))

        Text(
            text = "Lift cards will show up here",
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // horizontal divider
        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
    }
}