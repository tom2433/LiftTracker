package github.tom2433.lifttracker.ui.utils

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import github.tom2433.lifttracker.ui.viewModels.FilterState

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
    filterState: FilterState,
    onClickSessionNameDropdown: () -> Unit,
    dismissSessionNameDropdown: () -> Unit,
    onClickSessionNameDropdownItem: (Pair<String, Int>) -> Unit,
    onClickLoadMoreSessionNames: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showLoadMoreSessionNames =
        (filterState.totalNumberOfSessionNames > (filterState.sessionNameList.size - 1)) &&
                filterState.sessionNameList.isNotEmpty()

    SlideAndExpandSection(
        borderColor = borderColor,
        cardContentColor = cardContentColor,
        cardContainerColor = cardContainerColor,
        stage1Visible = filterState.filterSectionExpanded,
        stage2Visible = filterState.filterSectionStage2Expanded,
        modifier = modifier
    ) {
        // row to hold 'has session name' label and dropdown
        LabelAndDropdownRow(
            labelText = "Has session name:",
            borderColor = borderColor,
            selectedItem = filterState.selectedSessionName,
            itemList = filterState.sessionNameList,
            showLoadMoreItem = showLoadMoreSessionNames,
            dropdownExpanded = filterState.sessionNameDropdownExpanded,
            onClickDropdownItem = { onClickSessionNameDropdownItem(it) },
            onClickLoadMoreItem = onClickLoadMoreSessionNames,
            onClickDropdown = onClickSessionNameDropdown,
            dismissDropdown = dismissSessionNameDropdown
        )
    }
}