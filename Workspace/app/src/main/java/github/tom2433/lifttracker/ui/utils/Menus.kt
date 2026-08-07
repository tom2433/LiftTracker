package github.tom2433.lifttracker.ui.utils

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import github.tom2433.lifttracker.ui.viewModels.FilterState
import github.tom2433.lifttracker.ui.viewModels.FilterType

@Composable
fun ThreeDotMenu(
    @StringRes contentDescRes: Int,
    @StringRes element1TextRes: Int,
    @StringRes element2TextRes: Int,
    expanded: Boolean,
    onClickDots: () -> Unit,
    onClickElement1: () -> Unit,
    onClickElement2: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes element3TextRes: Int? = null,
    onClickElement3: () -> Unit = {}
) {
    // box to hold 3 dot menu
    Box(modifier = modifier) {
        // three dot icon
        IconButton(
            onClick = onClickDots
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(contentDescRes)
            )
        }

        // drop down menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest
        ) {
            // menu item 1
            DropdownMenuItem(
                text = { Text(stringResource(element1TextRes)) },
                onClick = onClickElement1
            )

            // menu item 2
            DropdownMenuItem(
                text = { Text(stringResource(element2TextRes)) },
                onClick = onClickElement2
            )

            // optional menu item 3
            if (element3TextRes != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(element3TextRes)) },
                    onClick = onClickElement3
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterMenu(
    borderColor: Color,
    cardContentColor: Color,
    cardContainerColor: Color,
    stage1Visible: Boolean,
    stage2Visible: Boolean,
    filterStatesMap: Map<FilterType, FilterState>,
    onClickFilterDropdown: (FilterType) -> Unit,
    dismissFilterDropdown: (FilterType) -> Unit,
    filterApplied: (FilterType, Pair<String, Int>) -> Unit,
    filterRemoved: (FilterType) -> Unit,
    loadMoreFilterElements: (FilterType) -> Unit,
    currentNoteText: String,
    onNoteChanged: (String) -> Unit,
    onDoneNote: () -> Unit,
    anyCountForNote: Int,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    SlideAndExpandSection(
        borderColor = borderColor,
        cardContentColor = cardContentColor,
        cardContainerColor = cardContainerColor,
        stage1Visible = stage1Visible,
        stage2Visible = stage2Visible,
        modifier = modifier
    ) {
        for ((filterType, filterState) in filterStatesMap) {
            // determine what to display for the selected item
            val selectedItem: Pair<String, Int> =
                if (filterState.selectedElementName == null) {
                    Pair("Any", filterState.anyCount)
                } else {
                    filterState.elementList.firstOrNull { it.first == filterState.selectedElementName }
                        ?: Pair(filterState.selectedElementName, 0)
                }

            // row to hold filter label and dropdown
            LabelAndDropdownRow(
                labelText = filterType.label,
                borderColor = borderColor,
                selectedItem = selectedItem,
                itemList = filterState.elementList,
                showLoadMoreItem =
                    ((filterState.totalNumberOfElements > (filterState.elementList.size)) &&
                    filterState.elementList.isNotEmpty()),
                dropdownExpanded = filterState.dropdownExpanded,
                defaultElementLabel = filterType.defaultElementLabel,
                anyCount = filterState.anyCount,
                filterApplied = { filterApplied(filterType, it) },
                filterRemoved = { filterRemoved(filterType) },
                loadMore = { loadMoreFilterElements(filterType) },
                onClickDropdown = { onClickFilterDropdown(filterType) },
                dismissDropdown = { dismissFilterDropdown(filterType) },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // row to hold note filter label and textfield
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth()
        ) {
            // note filter label
            Text(
                text = "Contains note with text:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(
                        top = 8.dp,
                        end = 16.dp
                    )
            )

            // textfield for search text
            OutlinedTextField(
                value = currentNoteText,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = cardContentColor,
                    unfocusedTextColor = cardContentColor,
                    focusedLabelColor = cardContentColor,
                    unfocusedLabelColor = cardContentColor,
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    cursorColor = cardContentColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                onValueChange = onNoteChanged,
                label = {
                    Text(
                        text = "Note text (${anyCountForNote})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onDoneNote()
                    }
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}