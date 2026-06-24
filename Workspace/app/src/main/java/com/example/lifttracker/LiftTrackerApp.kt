package com.example.lifttracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.lifttracker.ui.navigation.LiftTrackerNavHost
import kotlinx.coroutines.launch

/**
 * Top level composable that represents screens for the application
 */
@Composable
fun LiftTrackerApp(navController: NavHostController = rememberNavController()) {
    LiftTrackerNavHost(navController = navController)
}

/**
 * Modal Navigation drawer to be displayed unconditionally
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiftTrackerDrawer(
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
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

                    // nav drawer element: Begin Session
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.begin_session_title)) },
                        selected = false,
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
                        onClick = { /* TODO: Implement begin session screen */ }
                    )

                    // nav drawer element: Muscle Groups
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.muscle_groups_title)) },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.FitnessCenter,
                                contentDescription = stringResource(R.string.muscle_groups_title)
                            )
                        },
                        badge = {},
                        onClick = { /* TODO: Implement Muscle Groups Screen */ }
                    )

                    // nav drawer element: Sessions
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.sessions_title)) },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = stringResource(R.string.sessions_title)
                            )
                        },
                        badge = {},
                        onClick = { /* TODO: Implement Sessions screen */ }
                    )

                    // nav drawer element: Calendar
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.calendar_title)) },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = stringResource(R.string.calendar_title)
                            )
                        },
                        badge = {},
                        onClick = { /* TODO: Implement Calendar screen */ }
                    )

                    // nav drawer element: Switch Profile
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
                        onClick = { /* TODO: Implement Switch Profile Screen */ }
                    )

                    // spacer to separate above elements from settings
                    Spacer(modifier = Modifier.weight(1f))

                    // nav drawer element: Settings
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.settings_title)) },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.settings_title)
                            )
                        },
                        badge = {},
                        onClick = { /* TODO: Implement Settings Screen */ }
                    )
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
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
    }
}