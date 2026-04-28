package com.schedule.application

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.schedule.application.ui.AppViewModelFactory
import com.schedule.application.ui.RootViewModel
import com.schedule.application.ui.admin.AdminScreen
import com.schedule.application.ui.admin.AdminViewModel
import com.schedule.application.ui.schedule.ScheduleScreen
import com.schedule.application.ui.schedule.ScheduleViewModel
import com.schedule.application.ui.settings.SettingsScreen
import com.schedule.application.ui.settings.SettingsViewModel
import com.schedule.application.ui.theme.ScheduleAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as ScheduleApplication).appContainer

        setContent {
            val rootViewModel: RootViewModel = viewModel(
                factory = AppViewModelFactory { RootViewModel(container.settingsRepository) }
            )
            val scheduleViewModel: ScheduleViewModel = viewModel(
                factory = AppViewModelFactory { ScheduleViewModel(container.scheduleRepository) }
            )
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = AppViewModelFactory { SettingsViewModel(container.settingsRepository) }
            )
            val adminViewModel: AdminViewModel = viewModel(
                factory = AppViewModelFactory { AdminViewModel(container.adminRepository) }
            )

            val rootState by rootViewModel.state.collectAsStateWithLifecycle()

            ScheduleAppTheme(mode = rootState.settings.themeMode) {
                RequestNotificationPermissionIfNeeded()
                MainScaffold(
                    scheduleViewModel = scheduleViewModel,
                    settingsViewModel = settingsViewModel,
                    adminViewModel = adminViewModel,
                )
            }
        }
    }
}

@Composable
private fun MainScaffold(
    scheduleViewModel: ScheduleViewModel,
    settingsViewModel: SettingsViewModel,
    adminViewModel: AdminViewModel,
) {
    var tabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Schedule") },
                )
                NavigationBarItem(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                    label = { Text("Superuser") },
                )
                NavigationBarItem(
                    selected = tabIndex == 2,
                    onClick = { tabIndex = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (tabIndex) {
                0 -> ScheduleScreen(viewModel = scheduleViewModel, contentPadding = padding)
                1 -> AdminScreen(viewModel = adminViewModel, contentPadding = padding)
                2 -> SettingsScreen(viewModel = settingsViewModel, contentPadding = padding)
            }
        }
    }
}

@Composable
private fun RequestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT < 33) {
        return
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
