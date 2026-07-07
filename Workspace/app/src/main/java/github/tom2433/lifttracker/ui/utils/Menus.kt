package github.tom2433.lifttracker.ui.utils

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import github.tom2433.lifttracker.R

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
    modifier: Modifier = Modifier
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
            // menu item for edit
            DropdownMenuItem(
                text = { Text(stringResource(element1TextRes)) },
                onClick = onClickElement1
            )

            // menu item for delete
            DropdownMenuItem(
                text = { Text(stringResource(element2TextRes)) },
                onClick = onClickElement2
            )
        }
    }
}