package github.tom2433.lifttracker.ui.utils

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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

@Composable
fun FilterMenu(
    dividerColor: Color,
    cardContentColor: Color,
    cardContainerColor: Color,
    stage1Visible: Boolean,
    stage2Visible: Boolean,
    modifier: Modifier = Modifier
) {
    // column to hold all contents
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.End
    ) {
        // animated visibility for top divider
        AnimatedVisibility(
            visible = stage1Visible,
            enter = expandHorizontally(
                expandFrom = Alignment.End,
                animationSpec = tween(150)
            ),
            exit = shrinkHorizontally(
                shrinkTowards = Alignment.End,
                animationSpec = tween(150)
            )
        ) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = dividerColor
            )
        }

        // animated visibility for the interior content
        AnimatedVisibility(
            visible = stage2Visible,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = tween(300)
            ) + fadeIn(tween(300)),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(300)
            ) + fadeOut(tween(300))
        ) {
            // card to hold interior content
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors().copy(
                    contentColor = cardContentColor,
                    containerColor = cardContainerColor
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                // column to hold card content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Hello, World!")
                }
            }
        }

        // animated visibility for the bottom divider
        AnimatedVisibility(
            visible = stage2Visible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            )
        ) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = dividerColor
            )
        }
    }
}