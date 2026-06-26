package com.example.lifttracker

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.lifttracker.data.Profile
import com.example.lifttracker.ui.AppViewModelProvider
import com.example.lifttracker.ui.navigation.LiftTrackerNavHost
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
    content: @Composable (PaddingValues) -> Unit,
    viewModel: DrawerViewModel = viewModel(factory = AppViewModelProvider.Factory)
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
                            val shouldStayOpen = drawerOffsetX.value > -drawerUiState.drawerWidthPx * 0.1
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
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.PublishedWithChanges,
                            contentDescription = stringResource(R.string.switch_profile_title)
                        )
                    },
                    badge = {},
                    onClick = { /* TODO: Implement Switch Profile Dropdown */ }
                )

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
