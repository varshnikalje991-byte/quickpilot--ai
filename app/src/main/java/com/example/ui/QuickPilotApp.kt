package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PlanMyDayModal
import com.example.ui.components.QuickCaptureModal
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Slate400
import com.example.ui.theme.VioletPrimary
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPilotApp(viewModel: QuickPilotViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isCaptureModalOpen by viewModel.isCaptureModalOpen.collectAsState()
    val isPlanMyDayOpen by viewModel.isPlanMyDayModalOpen.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMoreMenu by remember { mutableStateOf(false) }

    // Handle back button for secondary screens
    if (currentScreen != AppNavDestination.HOME) {
        BackHandler {
            viewModel.currentScreen.value = AppNavDestination.HOME
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(VioletPrimary, CyanAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "QuickPilot AI",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Capture anything. Get organized.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.currentScreen.value = AppNavDestination.SEARCH },
                        modifier = Modifier.testTag("top_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (currentScreen == AppNavDestination.SEARCH) VioletPrimary else Slate400
                        )
                    }

                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "More Sections",
                                tint = if (currentScreen == AppNavDestination.NOTES || currentScreen == AppNavDestination.PROJECTS) VioletPrimary else Slate400
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Notes & Memory") },
                                onClick = {
                                    viewModel.currentScreen.value = AppNavDestination.NOTES
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null, tint = VioletPrimary) }
                            )
                            DropdownMenuItem(
                                text = { Text("Projects & Goals") },
                                onClick = {
                                    viewModel.currentScreen.value = AppNavDestination.PROJECTS
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, tint = CyanAccent) }
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.currentScreen.value = AppNavDestination.SETTINGS },
                        modifier = Modifier.testTag("top_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (currentScreen == AppNavDestination.SETTINGS) VioletPrimary else Slate400
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavIconItem(
                        icon = Icons.Outlined.Home,
                        selectedIcon = Icons.Default.Home,
                        label = "Home",
                        isSelected = currentScreen == AppNavDestination.HOME,
                        onClick = { viewModel.currentScreen.value = AppNavDestination.HOME }
                    )

                    NavIconItem(
                        icon = Icons.Outlined.Inbox,
                        selectedIcon = Icons.Default.Inbox,
                        label = "Inbox",
                        isSelected = currentScreen == AppNavDestination.INBOX,
                        onClick = { viewModel.currentScreen.value = AppNavDestination.INBOX }
                    )

                    // Glowing Center Floating Quick Capture Action
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(VioletPrimary, CyanAccent)
                                )
                            )
                            .clickable { viewModel.openCaptureModal() }
                            .testTag("floating_quick_capture_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Capture",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    NavIconItem(
                        icon = Icons.Outlined.Today,
                        selectedIcon = Icons.Default.Today,
                        label = "Today",
                        isSelected = currentScreen == AppNavDestination.TODAY,
                        onClick = { viewModel.currentScreen.value = AppNavDestination.TODAY }
                    )

                    NavIconItem(
                        icon = Icons.Outlined.CalendarMonth,
                        selectedIcon = Icons.Default.CalendarMonth,
                        label = "Calendar",
                        isSelected = currentScreen == AppNavDestination.CALENDAR,
                        onClick = { viewModel.currentScreen.value = AppNavDestination.CALENDAR }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                when (screen) {
                    AppNavDestination.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToToday = { viewModel.currentScreen.value = AppNavDestination.TODAY },
                        onNavigateToInbox = { viewModel.currentScreen.value = AppNavDestination.INBOX }
                    )
                    AppNavDestination.INBOX -> InboxScreen(viewModel = viewModel)
                    AppNavDestination.TODAY -> TodayScreen(viewModel = viewModel)
                    AppNavDestination.CALENDAR -> CalendarScreen(viewModel = viewModel)
                    AppNavDestination.NOTES -> NotesScreen(viewModel = viewModel)
                    AppNavDestination.PROJECTS -> ProjectsScreen(viewModel = viewModel)
                    AppNavDestination.SEARCH -> SearchScreen(viewModel = viewModel)
                    AppNavDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }

            // Quick Capture Modal Sheet
            if (isCaptureModalOpen) {
                QuickCaptureModal(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeCaptureModal() }
                )
            }

            // Plan My Day Modal Sheet
            if (isPlanMyDayOpen) {
                PlanMyDayModal(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closePlanMyDayModal() }
                )
            }
        }
    }
}

@Composable
fun NavIconItem(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else icon,
            contentDescription = label,
            tint = if (isSelected) VioletPrimary else Slate400,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
            color = if (isSelected) VioletPrimary else Slate400
        )
    }
}
