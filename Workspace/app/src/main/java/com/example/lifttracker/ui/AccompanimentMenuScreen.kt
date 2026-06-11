package com.example.lifttracker.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.lifttracker.R
import com.example.lifttracker.data.DataSource
import com.example.lifttracker.model.MenuItem
import com.example.lifttracker.model.MenuItem.AccompanimentItem
import com.example.lifttracker.ui.theme.LiftTrackerTheme

@Composable
fun AccompanimentMenuScreen(
    options: List<AccompanimentItem>,
    onCancelButtonClicked: () -> Unit,
    onNextButtonClicked: () -> Unit,
    onSelectionChanged: (AccompanimentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    @Suppress("UNCHECKED_CAST")
    BaseMenuScreen(
        options = options,
        onCancelButtonClicked = onCancelButtonClicked,
        onNextButtonClicked = onNextButtonClicked,
        onSelectionChanged = onSelectionChanged as (MenuItem) -> Unit,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun AccompanimentMenuPreview(){
    LiftTrackerTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AccompanimentMenuScreen(
                options = DataSource.accompanimentMenuItems,
                onNextButtonClicked = {},
                onCancelButtonClicked = {},
                onSelectionChanged = {},
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.padding_medium))
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}