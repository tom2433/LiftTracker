package com.example.lifttracker

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.lifttracker.ui.AppViewModelProvider
import com.example.lifttracker.ui.navigation.LiftTrackerNavHost
import com.example.lifttracker.ui.theme.LiftTrackerTheme
import com.example.lifttracker.ui.viewModels.DrawerViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Top level composable that represents screens for the application
 */
@Composable
fun LiftTrackerApp(navController: NavHostController = rememberNavController()) {
    LiftTrackerNavHost(navController = navController)
}

/**
 * Modal Navigation drawer to be displayed unconditionally
 *
 * Will eventually need parameters for:
 *  - sessionIsActive (Boolean)
 *
 * Might need to add a viewModel specifically for this Composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiftTrackerDrawer(
    @StringRes titleRes: Int,
    navigateToRecordSession: () -> Unit,
    navigateToMuscleGroups: () -> Unit,
    navigateToSessions: () -> Unit,
    navigateToCalendar: () -> Unit,
    navigateToAnalytics: () -> Unit,
    navigateToTools: () -> Unit,
    navigateToSettings: () -> Unit,
    viewModel: DrawerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    content: @Composable (PaddingValues) -> Unit,
) {
    val drawerUiState by viewModel.drawerUiState.collectAsState()
    val drawerOffsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(drawerUiState.isDrawerOpen, drawerUiState.drawerWidthPx) {
        if (drawerUiState.drawerWidthPx > 0f) {
            if (!drawerUiState.hasInitializedDrawerOffset) {
                drawerOffsetX.snapTo(if (drawerUiState.isDrawerOpen) 0f else -drawerUiState.drawerWidthPx)
                viewModel.initializeDrawerOffset()
            } else {
                drawerOffsetX.animateTo(
                    targetValue = if (drawerUiState.isDrawerOpen) 0f else -drawerUiState.drawerWidthPx,
                    animationSpec = tween(durationMillis = 700)
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(titleRes)) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                viewModel.toggleDrawer()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.menu)
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            content(innerPadding)
        }

        // scrim color animation for when drawer is closed or opened
        AnimatedVisibility(
            visible = drawerUiState.isDrawerOpen,
            enter = fadeIn(animationSpec = tween(durationMillis = 700)),
            exit = fadeOut(animationSpec = tween(durationMillis = 700)),
            modifier = Modifier.zIndex(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DrawerDefaults.scrimColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.closeDrawer()
                    }
            )
        }

        ModalDrawerSheet(
            modifier = Modifier
                .zIndex(2f)
                .width(300.dp)
                .onSizeChanged { size ->
                    viewModel.updateDrawerWidthPx(size.width.toFloat())
                }
                .offset {
                    IntOffset(drawerOffsetX.value.roundToInt(), 0)
                }
                // handle when the user drags the drawer with their finger
                .pointerInput(drawerUiState.drawerWidthPx) {
                    detectHorizontalDragGestures(
                        // drawer shall follow the user's touch input
                        onHorizontalDrag = { _, dragAmount ->
                            if (drawerUiState.drawerWidthPx > 0f) {
                                val newOffset = (drawerOffsetX.value + dragAmount)
                                    .coerceIn(-drawerUiState.drawerWidthPx, 0f)

                                coroutineScope.launch {
                                    drawerOffsetX.snapTo(newOffset)
                                }
                            }
                        },
                        onDragEnd = {
                            // drawer should stay open if the trailing edge is at more than 0.9
                            // of the original drawer width
                            val shouldStayOpen =
                                drawerOffsetX.value > -drawerUiState.drawerWidthPx * 0.1
                            viewModel.updateIsDrawerOpen(shouldStayOpen)

                            coroutineScope.launch {
                                drawerOffsetX.animateTo(
                                    targetValue = if (shouldStayOpen) 0f else -drawerUiState.drawerWidthPx,
                                    animationSpec = tween(durationMillis = 700)
                                )
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                drawerOffsetX.animateTo(
                                    targetValue = if (drawerUiState.isDrawerOpen) 0f else -drawerUiState.drawerWidthPx,
                                    animationSpec = tween(durationMillis = 700)
                                )
                            }
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp
                    )
                    .verticalScroll(rememberScrollState())
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
            ) {
                // nav drawer title
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )

                // divider to separate app name from drawer items
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // nav drawer element: Begin Session (eventually dynamic for resume/quit session)
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.begin_session_title)) },
                    selected = titleRes == R.string.record_session_title,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = stringResource(R.string.begin_session_title)
                        )
                    },
                    // placeholder for now.
                    // the badge is displayed all the way to the right, usually some light text.
                    // may or may not implement in the future.
                    badge = {},
                    onClick = {
                        coroutineScope.launch {
                            drawerOffsetX.animateTo(
                                targetValue = -drawerUiState.drawerWidthPx,
                                animationSpec = tween(durationMillis = 400)
                            )
                            navigateToRecordSession()
                        }
                    }
                )

                // nav drawer element: Muscle Groups
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.muscle_groups_title)) },
                    selected = titleRes == R.string.muscle_groups_title,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.FitnessCenter,
                            contentDescription = stringResource(R.string.muscle_groups_title)
                        )
                    },
                    badge = {},
                    onClick = {
                        coroutineScope.launch {
                            drawerOffsetX.animateTo(
                                targetValue = -drawerUiState.drawerWidthPx,
                                animationSpec = tween(durationMillis = 400)
                            )
                            navigateToMuscleGroups()
                        }
                    }
                )

                // nav drawer element: Sessions
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.sessions_title)) },
                    selected = titleRes == R.string.sessions_title,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = stringResource(R.string.sessions_title)
                        )
                    },
                    badge = {},
                    onClick = {
                        coroutineScope.launch {
                            drawerOffsetX.animateTo(
                                targetValue = -drawerUiState.drawerWidthPx,
                                animationSpec = tween(durationMillis = 400)
                            )
                            navigateToSessions()
                        }
                    }
                )

                // nav drawer element: Calendar
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.calendar_title)) },
                    selected = titleRes == R.string.calendar_screen_title,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = stringResource(R.string.calendar_title)
                        )
                    },
                    badge = {},
                    onClick = {
                        coroutineScope.launch {
                            drawerOffsetX.animateTo(
                                targetValue = -drawerUiState.drawerWidthPx,
                                animationSpec = tween(durationMillis = 400)
                            )
                            navigateToCalendar()
                        }
                    }
                )

                // TODO: nav drawer element: Switch Profile
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.switch_profile_title)) },
                    selected = drawerUiState.switchProfileSelected,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.PublishedWithChanges,
                            contentDescription = stringResource(R.string.switch_profile_title)
                        )
                    },
                    badge = {},
                    onClick = {
                        viewModel.toggleSwitchProfileSelected()
                    }
                )

                // display list of profiles if user selected to switch profiles
                if (drawerUiState.switchProfileSelected) {
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))

                    for (profile in drawerUiState.profileList) {
                        Row(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .wrapContentSize(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // card for profile name/note
                            Card(
                                modifier = Modifier
                                    .heightIn(min = 56.dp)
                                    .weight(5f)
                                    .clickable(
                                        onClick = { /* TODO: onClick switch to this profile if not active */ }
                                    ),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    bottomStart = 16.dp
                                ),
//                                colors = CardDefaults.cardColors(
//                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                                )
                            ) {
                                // put name and note here
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = profile.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    if (profile.note.isNotBlank()) {
                                        Text(
                                            text = profile.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // card for edit/delete
                            Card(
                                modifier = Modifier
                                    .heightIn(min = 56.dp)
                                    .weight(1f)
                                    .clickable(
                                        onClick = { /* TODO: onClick edit/delete this profile */ }
                                    ),
                                shape = RoundedCornerShape(
                                    topEnd = 16.dp,
                                    bottomEnd = 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (profile.active) {
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.errorContainer
                                    },
                                    contentColor = if (profile.active) {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )
                            ) {
                                // Trash can if not active, pencil if active
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (profile.active) {
                                            Icons.Filled.Edit
                                        } else {
                                            Icons.Filled.Delete
                                        },
                                        contentDescription = if (profile.active) {
                                            stringResource(R.string.edit_profile)
                                        } else {
                                            stringResource(R.string.delete_profile)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                }

                // divider to separate
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // nav drawer element: Analytics
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.analytics_title)) },
                    selected = titleRes == R.string.analytics_title,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Timeline,
                            contentDescription = stringResource(R.string.analytics_title)
                        )
                    },
                    badge = {},
                    onClick = {
                        coroutineScope.launch {
                            drawerOffsetX.animateTo(
                                targetValue = -drawerUiState.drawerWidthPx,
                                animationSpec = tween(durationMillis = 400)
                            )
                            navigateToAnalytics()
                        }
                    }
                )

                // nav drawer element: Tools
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.tools_title)) },
                    selected = titleRes == R.string.tools_title,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Handyman,
                            contentDescription = stringResource(R.string.tools_title)
                        )
                    },
                    badge = {},
                    onClick = {
                        coroutineScope.launch {
                            drawerOffsetX.animateTo(
                                targetValue = -drawerUiState.drawerWidthPx,
                                animationSpec = tween(durationMillis = 400)
                            )
                            navigateToTools()
                        }
                    }
                )

                // nav drawer element: Settings
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.settings_title)) },
                    selected = titleRes == R.string.settings_title,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    },
                    badge = {},
                    onClick = {
                        coroutineScope.launch {
                            drawerOffsetX.animateTo(
                                targetValue = -drawerUiState.drawerWidthPx,
                                animationSpec = tween(durationMillis = 400)
                            )
                            navigateToSettings()
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LiftTrackerDrawerPreview() {
    LiftTrackerTheme(dynamicColor = false, darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LiftTrackerDrawer(
                titleRes = R.string.app_name,
                navigateToRecordSession = {},
                navigateToMuscleGroups = {},
                navigateToSessions = {},
                navigateToCalendar = {},
                navigateToAnalytics = {},
                navigateToTools = {},
                navigateToSettings = {},
                content = {},
            )
        }
    }
}
