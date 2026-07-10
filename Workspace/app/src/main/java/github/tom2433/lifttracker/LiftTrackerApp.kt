package github.tom2433.lifttracker

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import github.tom2433.lifttracker.ui.AppViewModelProvider
import github.tom2433.lifttracker.ui.navigation.LiftTrackerNavHost
import github.tom2433.lifttracker.ui.screens.AnalyticsDestination
import github.tom2433.lifttracker.ui.screens.CalendarDestination
import github.tom2433.lifttracker.ui.screens.MuscleGroupsDestination
import github.tom2433.lifttracker.ui.screens.RecordSessionDestination
import github.tom2433.lifttracker.ui.screens.SessionsDestination
import github.tom2433.lifttracker.ui.screens.SettingsDestination
import github.tom2433.lifttracker.ui.screens.ToolsDestination
import github.tom2433.lifttracker.ui.utils.ShowElementDeleteDialog
import github.tom2433.lifttracker.ui.utils.ShowElementEntryDialog
import github.tom2433.lifttracker.ui.viewModels.DrawerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Top level composable that represents screens for the application
 */
@Composable
fun LiftTrackerApp(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val layoutDirection = LocalLayoutDirection.current

    val titleRes = when (currentRoute) {
        MuscleGroupsDestination.route -> MuscleGroupsDestination.titleRes
        RecordSessionDestination.route -> RecordSessionDestination.titleRes
        SessionsDestination.route -> SessionsDestination.titleRes
        CalendarDestination.route -> CalendarDestination.titleRes
        AnalyticsDestination.route -> AnalyticsDestination.titleRes
        ToolsDestination.route -> ToolsDestination.titleRes
        SettingsDestination.route -> SettingsDestination.titleRes
        else -> R.string.app_name
    }

    LiftTrackerDrawer(
        titleRes = titleRes,
        navigateToRecordSession = { navController.navigate(RecordSessionDestination.route) },
        navigateToMuscleGroups = { navController.navigate(MuscleGroupsDestination.route) },
        navigateToSessions = { navController.navigate(SessionsDestination.route) },
        navigateToCalendar = { navController.navigate(CalendarDestination.route) },
        navigateToAnalytics = { navController.navigate(AnalyticsDestination.route) },
        navigateToTools = { navController.navigate(ToolsDestination.route) },
        navigateToSettings = { navController.navigate(SettingsDestination.route) },
    ) { innerPadding ->
        LiftTrackerNavHost(
            navController = navController,
            modifier = Modifier.padding(
                paddingValues = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    bottom = 0.dp,
                    end = innerPadding.calculateEndPadding(layoutDirection)
                )
            )
        )
    }
}

/**
 * Modal Navigation drawer to be displayed unconditionally
 *
 * Will eventually need parameters for:
 *  - sessionIsActive (Boolean)
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
    viewModel.checkScreenForFab(titleRes)
    val beginSessionNavElementColors = if (drawerUiState.activeLiftDay == null) {
        NavigationDrawerItemDefaults.colors()
    } else {
        NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            unselectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }

    LaunchedEffect(drawerUiState.isDrawerOpen, drawerUiState.drawerWidthPx) {
        if (drawerUiState.drawerWidthPx > 0f) {
            if (!drawerUiState.hasInitializedDrawerOffset) {
                drawerOffsetX.snapTo(if (drawerUiState.isDrawerOpen) 0f else -drawerUiState.drawerWidthPx)
                viewModel.initializeDrawerOffset()
            } else {
                drawerOffsetX.animateTo(
                    targetValue = if (drawerUiState.isDrawerOpen) 0f else -drawerUiState.drawerWidthPx,
                    animationSpec = tween(durationMillis = 500)
                )
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // box to hold full scaffold
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(titleRes)) },
                    scrollBehavior = scrollBehavior,
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
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = drawerUiState.showFab,
                    enter = slideInVertically(
                        initialOffsetY = { it * 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { it * 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeOut()
                ) {
                    FloatingActionButton(
                        onClick = {
                            viewModel.showAddMuscleGroupDialog()
                        },
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "" // TODO
                        )
                    }
                }
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

        // full drawer sheet
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

                // nav drawer element: Begin/Resume Session
                NavigationDrawerItem(
                    label = {
                        if (drawerUiState.activeLiftDay == null) {
                            Text(stringResource(R.string.begin_session_title))
                        } else {
                            Text(stringResource(R.string.resume_session))
                        }
                    },
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
                            viewModel.closeDrawer()
                            delay(200)
                            navigateToRecordSession()
                        }
                    },
                    colors = beginSessionNavElementColors
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
                            viewModel.closeDrawer()
                            delay(200)
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
                            viewModel.closeDrawer()
                            delay(200)
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
                            viewModel.closeDrawer()
                            delay(200)
                            navigateToCalendar()
                        }
                    }
                )

                // nav drawer element: Switch Profiles dropdown
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
                AnimatedVisibility(
                    visible = drawerUiState.switchProfileSelected,
                    enter = expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(),
                    exit = shrinkVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(
                                start = 16.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            )
                        )

                        for (profile in drawerUiState.profileList) {
                            Row(
                                modifier = Modifier
                                    .padding(start = 16.dp, bottom = 8.dp)
                                    .height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // card for profile name/note
                                Card(
                                    modifier = Modifier
                                        .weight(3f)
                                        .defaultMinSize(minHeight = 56.dp),
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        bottomStart = 16.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (profile.active) {
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainer
                                        },
                                        contentColor = if (profile.active) {
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    ),
                                    onClick = { viewModel.changeActiveProfile(profile) }
                                ) {
                                    // put name and note here
                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.Start,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp)
                                    ) {
                                        Text(
                                            text = profile.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        if (profile.note.isNotBlank()) {
                                            Text(
                                                text = profile.note,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (profile.active) {
                                                    MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                                        alpha = 0.75f
                                                    )
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.75f
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }

                                // card for edit/delete
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .defaultMinSize(minHeight = 56.dp),
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
                                    ),
                                    onClick = {
                                        if (profile.active) {
                                            viewModel.editProfileBtnClicked(profileToEdit = profile)
                                        } else {
                                            viewModel.setProfileToDelete(deletedProfile = profile)
                                        }
                                    }
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

                        // add button to add new profile
                        Card(
                            modifier = Modifier
                                .padding(start = 16.dp, bottom = 8.dp)
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            onClick = { viewModel.addProfileBtnClicked() }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = stringResource(R.string.create_profile_btn_text)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                    }
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
                            viewModel.closeDrawer()
                            delay(200)
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
                            viewModel.closeDrawer()
                            delay(200)
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
                            viewModel.closeDrawer()
                            delay(200)
                            navigateToSettings()
                        }
                    }
                )
            }
        }

        // profile entry dialog
        if (drawerUiState.profileEntryDialogVisible) {
            ShowElementEntryDialog(
                dialogTitle = if (drawerUiState.userIsAddingProfile) {
                    stringResource(R.string.create_profile_btn_text)
                } else {
                    stringResource(R.string.edit_profile)
                },
                submitBtnText = if (drawerUiState.userIsAddingProfile) {
                    stringResource(R.string.create_profile_btn_text)
                } else {
                    stringResource(R.string.update_profile_btn_text)
                },
                elementNameInputLabel = stringResource(R.string.profile_name_input_label),
                elementNoteInputLabel = stringResource(R.string.profile_note_input_label),
                buttonEnabled = viewModel.isProfileValid(),
                newElementName = drawerUiState.newProfileName,
                newElementNote = drawerUiState.newProfileNote,
                onElementNameValueChanged = {
                    viewModel.updateNewProfileName(it)
                },
                onElementNoteValueChanged = {
                    viewModel.updateNewProfileNote(it)
                },
                onSubmit = {
                    viewModel.submitProfileEntryDialog()
                },
                onDismissRequest = {
                    viewModel.dismissProfileEntryDialog()
                }
            )
        }

        // delete profile dialog
        if (drawerUiState.deleteProfileDialogVisible) {
            ShowElementDeleteDialog(
                dialogTitle = "Delete \"${drawerUiState.profileToDelete?.name ?: "null (something bad happend. help)"} \"?",
                warningDescription = R.string.delete_profile_warning,
                deleteBtnText = R.string.delete_profile_btn_text,
                onDismissRequest = {
                    viewModel.dismissDeleteProfileDialog()
                },
                onDelete = {
                    viewModel.deleteProfile()
                },
            )
        }

        // add muscle group dialog
        if (drawerUiState.muscleGroupEntryDialogVisible) {
            ShowElementEntryDialog(
                dialogTitle = stringResource(R.string.add_muscle_group),
                submitBtnText = stringResource(R.string.create_muscle_group),
                elementNameInputLabel = stringResource(R.string.muscle_group_name),
                elementNoteInputLabel = stringResource(R.string.muscle_group_note),
                buttonEnabled = viewModel.isMuscleGroupValid(),
                newElementName = drawerUiState.newMuscleGroupName,
                newElementNote = drawerUiState.newMuscleGroupNote,
                onElementNameValueChanged = {
                    viewModel.updateNewMuscleGroupName(it)
                },
                onElementNoteValueChanged = {
                    viewModel.updateNewMuscleGroupNote(it)
                },
                onSubmit = {
                    viewModel.submitMuscleGroupEntryDialog()
                },
                onDismissRequest = {
                    viewModel.dismissMuscleGroupEntryDialog()
                }
            )
        }
    }
}
